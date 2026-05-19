const { spawn } = require('child_process');
const path = require('path');

const serverDir = path.join(__dirname, '..', 'server');
const clientDir = path.join(__dirname, '..', 'client');

// Start Java Spring Boot server via Maven Wrapper
const server = spawn('./mvnw', ['spring-boot:run'], {
  cwd: serverDir,
  stdio: 'inherit',
  env: { ...process.env }
});

// Start Vite dev server for the React frontend
const client = spawn('npx', ['vite', '--port', '3000'], {
  cwd: clientDir,
  stdio: 'inherit',
  env: { ...process.env }
});

process.on('SIGINT', () => {
  server.kill();
  client.kill();
  process.exit();
});

process.on('SIGTERM', () => {
  server.kill();
  client.kill();
  process.exit();
});
