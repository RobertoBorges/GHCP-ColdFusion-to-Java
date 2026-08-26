#!/usr/bin/env node
/**
 * capture-screenshots.mjs — Visual baseline capture for a running legacy ColdFusion app.
 *
 * Part of the "visual-baseline-capture" skill (Phase 2). It logs into the running legacy app once,
 * reuses the session, and takes FULL-PAGE screenshots of each route listed in a routes.json file,
 * then writes a manifest.json cataloging every shot. Phase 3 opens these screenshots as a visual
 * spec when generating Thymeleaf templates.
 *
 * ----------------------------------------------------------------------------------------------
 * Prerequisites (run once, in the folder where you invoke this script):
 *   npm init -y                 # only if there is no package.json yet
 *   npm i -D playwright
 *   npx playwright install chromium
 *
 * Usage:
 *   BASE_URL=http://localhost:8080 VB_USERNAME=admin VB_PASSWORD=admin \
 *     node capture-screenshots.mjs routes.json
 *
 * On Windows PowerShell:
 *   $env:BASE_URL="http://localhost:8080"; $env:VB_USERNAME="admin"; $env:VB_PASSWORD="admin"
 *   node capture-screenshots.mjs routes.json
 * ----------------------------------------------------------------------------------------------
 *
 * routes.json is an array of route objects (see routes.example.json). Only the fields the script
 * needs are read; the rest are copied through to the manifest:
 *   { "id", "path", "sourcePage", "authState", "params", "viewport", "states", "notes", "behaviorNotes" }
 *
 * Environment variables (all optional except credentials when authenticated routes exist):
 *   BASE_URL            Base URL of the running app                (default http://localhost:8080)
 *   VB_OUT              Output folder for screenshots + manifest   (default . — the script's own dir)
 *   VB_USERNAME         Login username
 *   VB_PASSWORD         Login password
 *   VB_LOGIN_PATH       Path of the login page/form target         (default /index.cfm)
 *   VB_USER_SELECTOR    Username field selector                    (default input[name="username"])
 *   VB_PASS_SELECTOR    Password field selector                    (default input[name="password"])
 *   VB_SUBMIT_SELECTOR  Submit control selector                    (default input[type=submit], button[type=submit], button:has-text("Login"))
 *   VB_LOGGED_IN_TEXT   Text present only when logged in (verifies login)   (default: none)
 *   VB_DESKTOP_WIDTH    Desktop viewport width in px               (default 1280)
 *   VB_NAV_TIMEOUT_MS   Per-page navigation timeout                (default 30000)
 */

import { chromium, devices } from 'playwright';
import { fileURLToPath } from 'node:url';
import { dirname, resolve, join } from 'node:path';
import { readFile, writeFile, mkdir } from 'node:fs/promises';

const scriptDir = dirname(fileURLToPath(import.meta.url));

const BASE_URL = (process.env.BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');
const OUT_DIR = resolve(process.env.VB_OUT || scriptDir);
const USERNAME = process.env.VB_USERNAME || '';
const PASSWORD = process.env.VB_PASSWORD || '';
const LOGIN_PATH = process.env.VB_LOGIN_PATH || '/index.cfm';
const USER_SELECTOR = process.env.VB_USER_SELECTOR || 'input[name="username"]';
const PASS_SELECTOR = process.env.VB_PASS_SELECTOR || 'input[name="password"]';
const SUBMIT_SELECTOR =
  process.env.VB_SUBMIT_SELECTOR || 'input[type=submit], button[type=submit], button:has-text("Login")';
const LOGGED_IN_TEXT = process.env.VB_LOGGED_IN_TEXT || '';
const DESKTOP_WIDTH = parseInt(process.env.VB_DESKTOP_WIDTH || '1280', 10);
const NAV_TIMEOUT = parseInt(process.env.VB_NAV_TIMEOUT_MS || '30000', 10);

const routesArg = process.argv[2] || join(OUT_DIR, 'routes.json');

function log(msg) {
  process.stdout.write(`[visual-baseline] ${msg}\n`);
}

async function loadRoutes(path) {
  const raw = await readFile(resolve(path), 'utf8');
  const routes = JSON.parse(raw);
  if (!Array.isArray(routes)) throw new Error(`${path} must contain a JSON array of route objects`);
  return routes;
}

async function login(context) {
  if (!USERNAME) {
    log('No VB_USERNAME set — skipping login (public capture only).');
    return false;
  }
  const page = await context.newPage();
  const loginUrl = BASE_URL + LOGIN_PATH;
  log(`Logging in at ${loginUrl} as "${USERNAME}"...`);
  await page.goto(loginUrl, { waitUntil: 'domcontentloaded', timeout: NAV_TIMEOUT });
  await page.fill(USER_SELECTOR, USERNAME);
  await page.fill(PASS_SELECTOR, PASSWORD);
  await Promise.all([
    page.waitForLoadState('networkidle', { timeout: NAV_TIMEOUT }).catch(() => {}),
    page.click(SUBMIT_SELECTOR),
  ]);
  let ok = true;
  if (LOGGED_IN_TEXT) {
    ok = await page.locator(`text=${LOGGED_IN_TEXT}`).first().isVisible().catch(() => false);
  } else {
    // Heuristic: consider login successful if the password field is no longer present.
    ok = !(await page.locator(PASS_SELECTOR).first().isVisible().catch(() => false));
  }
  await page.close();
  log(ok ? 'Login appears successful.' : 'WARNING: login could not be verified — check credentials/selectors.');
  return ok;
}

async function captureRoute(context, route, isMobile) {
  const path = route.path || route.url || '/';
  const url = BASE_URL + (path.startsWith('/') ? path : '/' + path);
  const fileName = isMobile ? `${route.id}-mobile.png` : `${route.id}.png`;
  const dest = join(OUT_DIR, fileName);
  const page = await context.newPage();
  const entry = {
    id: route.id,
    sourcePage: route.sourcePage || path.replace(/^\//, ''),
    url: path,
    authState: route.authState || 'public',
    params: route.params ?? null,
    viewport: isMobile ? 'mobile' : `${DESKTOP_WIDTH}-full`,
    screenshot: fileName,
    states: route.states || ['default'],
    notes: route.notes || '',
    behaviorNotes: route.behaviorNotes || '',
    targetView: '',
    captured: 'ok',
  };
  try {
    await page.goto(url, { waitUntil: 'domcontentloaded', timeout: NAV_TIMEOUT });
    await page.waitForLoadState('networkidle', { timeout: NAV_TIMEOUT }).catch(() => {});
    await page.screenshot({ path: dest, fullPage: true });
    log(`captured ${fileName}  (${url})`);
  } catch (err) {
    entry.captured = `error: ${err.message}`;
    log(`FAILED  ${fileName}  (${url}) — ${err.message}`);
  } finally {
    await page.close();
  }
  return entry;
}

async function main() {
  await mkdir(OUT_DIR, { recursive: true });
  const routes = await loadRoutes(routesArg);
  log(`${routes.length} route(s) from ${routesArg}; base URL ${BASE_URL}; output ${OUT_DIR}`);

  const browser = await chromium.launch();

  // Public pages are captured WITHOUT a session; authenticated pages use a logged-in context — so the
  // public login page and the post-login screens each render correctly (they may share a URL).
  const publicDesktop = await browser.newContext({ viewport: { width: DESKTOP_WIDTH, height: 900 } });
  const needsAuth = routes.some((r) => (r.authState || 'public') !== 'public');
  let authDesktop = null;
  if (needsAuth) {
    authDesktop = await browser.newContext({ viewport: { width: DESKTOP_WIDTH, height: 900 } });
    await login(authDesktop);
  }
  const isPublic = (route) => (route.authState || 'public') === 'public';
  const desktopFor = (route) => (isPublic(route) ? publicDesktop : authDesktop || publicDesktop);

  const manifest = [];
  for (const route of routes) {
    if (!route.id) {
      log(`skipping route without an "id": ${JSON.stringify(route)}`);
      continue;
    }
    manifest.push(await captureRoute(desktopFor(route), route, false));
    if (route.viewport === 'mobile') {
      const mobileCtx = await browser.newContext({ ...devices['iPhone 13'] });
      if (!isPublic(route) && USERNAME) await login(mobileCtx);
      manifest.push(await captureRoute(mobileCtx, route, true));
      await mobileCtx.close();
    }
  }

  await publicDesktop.close();
  if (authDesktop) await authDesktop.close();
  await browser.close();

  const manifestPath = join(OUT_DIR, 'manifest.json');
  await writeFile(manifestPath, JSON.stringify(manifest, null, 2) + '\n', 'utf8');
  const okCount = manifest.filter((m) => m.captured === 'ok').length;
  log(`Wrote ${manifestPath} — ${okCount}/${manifest.length} captured OK.`);
  if (okCount < manifest.length) process.exitCode = 1;
}

main().catch((err) => {
  console.error(`[visual-baseline] fatal: ${err.stack || err.message}`);
  process.exit(1);
});
