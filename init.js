const args = process.argv.slice(2);
const fs = require('fs');
const { execSync } = require('child_process');
const crypto = require('crypto');
const path = require('path');
const { warn, deleteDir, copyDir, log, deleteFile, cacheExpired, error } = require('./config');

const stdio = [process.stdin, process.stdout, process.stderr];

const DECOMPILE_VERSION = args[0] ?? "1.20.2";

let MINECRAFT_VERSION = "UNKNOWN";

(async function () {
  try {
    console.time('Download server');
    const meta = await downloadServer();
    console.timeEnd('Download server');
    console.time('Remap server');
    await remapServer(meta.id);
    console.timeEnd('Remap server');
    console.time('Decompile');
    if (fs.existsSync('compiled')) deleteDir('compiled');
    if (fs.existsSync('decompiled')) deleteDir('decompiled');
    fs.mkdirSync('compiled');
    fs.mkdirSync('decompiled');
    warn('Extracting server.jar, this may take a while...');
    execSync(`jar xf server.jar net com`, { stdio });
    warn('Copying net and com directories, this may take a while...');
    fs.mkdirSync('compiled/net', { recursive: true });
    fs.mkdirSync('compiled/com', { recursive: true });
    copyDir('net', 'compiled');
    copyDir('com', 'compiled');
    deleteDir('net');
    deleteDir('com');
    execSync(`java -jar ./libs/fernflower.jar -dgs=1 -hdc=0 -asc=1 -udv=0 -rsy=1 -aoa=1 ${path.join(__dirname, 'compiled')} ${path.join(__dirname, 'decompiled')}`, { stdio });
    console.timeEnd('Decompile');
    log('Cleaning up compiled sources');
    deleteDir('compiled');
    deleteFile('server.jar');
  } catch (e) {
    console.error(e);
    return process.exit(1);
  }
  process.exit(0);
})();

async function downloadServer() {
  let manifest;
  if (!fs.existsSync('cache')) fs.mkdirSync('cache');
  if (fs.existsSync('cache/manifest.json') && !cacheExpired('cache/manifest.json')) {
    manifest = JSON.parse(fs.readFileSync('cache/manifest.json').toString());
  } else {
    manifest = await (await fetch('https://launchermeta.mojang.com/mc/game/version_manifest_v2.json')).json();
    fs.writeFileSync('cache/manifest.json', JSON.stringify(manifest));
  }

  const index = manifest.versions.findIndex(a => a.id == DECOMPILE_VERSION);
  if (index == -1) index = 0;
  const version = manifest.versions[index].id;
  MINECRAFT_VERSION = version;
  warn(`Found Minecraft version '${version}' with the release type '${manifest.versions[index].type}'`);
  let meta;
  if (fs.existsSync(`cache/${version}.json`)) {
    meta = JSON.parse(fs.readFileSync(`cache/${version}.json`).toString());
  } else {
    meta = await (await fetch(manifest.versions[index].url)).json();
    fs.writeFileSync(`cache/${manifest.versions[index].id}.json`, JSON.stringify(meta));
  }

  if (!fs.existsSync(`cache/${version}`)) fs.mkdirSync(`cache/${version}`);

  if (fs.existsSync(`cache/${version}/server.jar`)) {
    const hash = calculateHash(`cache/${version}/server.jar`);
    if (hash !== meta.downloads.server.sha1) {
      warn(`Server jar sha1 hash does not match!\nFile Hash: ${hash}\nMeta: ${meta.downloads.server.sha1}`);
      warn('Deleting cache and re-installing...');
      deleteDir(`cache/${version}`);
      return await downloadServer();
    }
  } else {
    execSync(`curl -o cache/${version}/server.jar ${meta.downloads.server.url}`, { stdio });
  }

  if (fs.existsSync(`cache/${version}/mappings.txt`)) {
    const hash = calculateHash(`cache/${version}/mappings.txt`);
    if (hash !== meta.downloads.server_mappings.sha1) {
      warn(`Server mappings sha1 hash does not match!\nFile Hash: ${hash}\nMeta: ${meta.downloads.server_mappings.sha1}`);
      warn('Deleting cache and re-installing...');
      deleteDir(`cache/${version}`);
      return await downloadServer();
    }
  } else {
    execSync(`curl -o cache/${version}/mappings.txt ${meta.downloads.server_mappings.url}`, { stdio });
  }

  if (!fs.existsSync(`cache/${version}/server-${version}.jar`)) {
    execSync(`cd cache/${version} && jar xf server.jar META-INF`, { stdio });
    fs.copyFileSync(`cache/${version}/META-INF/versions/${version}/server-${version}.jar`, `cache/${version}/server-${version}.jar`);
  }

  return meta;
}

async function remapServer(version) {
  execSync(`java -jar ./libs/specialsource.jar -i ./cache/${version}/server-${version}.jar -m ./cache/${version}/mappings.txt -o server.jar`, { stdio });
}

function calculateHash(file, type) {
  return crypto.createHash('sha1').update(fs.readFileSync(file)).digest("hex");
}
