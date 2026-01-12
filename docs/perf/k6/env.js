function loadEnvFile(path) {
  const env = {};
  try {
    const content = open(path);
    content.split('\n').forEach((line) => {
      const trimmed = line.trim();
      if (!trimmed || trimmed.startsWith('#')) {
        return;
      }
      const idx = trimmed.indexOf('=');
      if (idx === -1) {
        return;
      }
      const key = trimmed.slice(0, idx).trim();
      const value = trimmed.slice(idx + 1).trim();
      env[key] = value;
    });
  } catch (err) {
    // Missing env file is fine; fall back to process env and defaults.
  }
  return env;
}

const ENV_FILE = __ENV.K6_ENV_FILE || './.env';
const FILE_ENV = loadEnvFile(ENV_FILE);

export function getEnv(key, fallback) {
  return __ENV[key] || FILE_ENV[key] || fallback;
}
