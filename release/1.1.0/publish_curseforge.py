import hashlib
import json
import os
from pathlib import Path
import sys
import urllib.error
import urllib.request
import uuid

root = Path(__file__).parent
jar = root / 'alchemical-lasers-1.1.0-neoforge-1.21.1.jar'
token = os.environ.get('CURSEFORGE_TOKEN', '').strip()
if not token:
    sys.exit('CURSEFORGE_TOKEN is not configured for this repository. No upload attempted.')
content = jar.read_bytes()
if hashlib.sha256(content).hexdigest() != 'f97aa0a3b1fa139c144c84f05a4223fcda473e02085ad57c10d17b16441be55e':
    sys.exit('Verified JAR checksum mismatch. No upload attempted.')
metadata = {
    'displayName': 'Create: Alchemical Lasers 1.1.0 — NeoForge 1.21.1',
    'changelog': (root / 'Create-Alchemical-Lasers-1.1.0-Changelog.html').read_text(),
    'changelogType': 'html',
    'releaseType': 'release',
    'gameVersionNames': ['1.21.1', 'NeoForge', 'Java 21'],
    'relations': {'projects': [{'slug': 'create', 'projectID': '328085', 'type': 'requiredDependency'}]},
}
boundary = '----AlchemyUpload' + uuid.uuid4().hex
body = (
    f'--{boundary}\r\nContent-Disposition: form-data; name="metadata"\r\n\r\n'.encode()
    + json.dumps(metadata).encode()
    + f'\r\n--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="{jar.name}"\r\nContent-Type: application/java-archive\r\n\r\n'.encode()
    + content
    + f'\r\n--{boundary}--\r\n'.encode()
)
request = urllib.request.Request(
    'https://minecraft.curseforge.com/api/projects/1787221/upload-file',
    data=body,
    headers={'X-Api-Token': token, 'Content-Type': 'multipart/form-data; boundary=' + boundary, 'User-Agent': 'Alchemical-Lasers-Release/1.1.0'},
    method='POST',
)
try:
    with urllib.request.urlopen(request, timeout=180) as response:
        result = json.load(response)
except urllib.error.HTTPError as error:
    print('CurseForge rejected upload: HTTP', error.code)
    print(error.read().decode(errors='replace').replace(token, '[REDACTED]'))
    raise SystemExit(1)
file_id = result.get('id')
if not isinstance(file_id, int) or file_id <= 0:
    sys.exit('Upload response did not include a valid file ID: ' + json.dumps(result))
result['fileUrl'] = f'https://www.curseforge.com/minecraft/mc-mods/create-alchemical-lasers/files/{file_id}'
(root / 'publish-result.json').write_text(json.dumps(result, indent=2))
print('CurseForge upload succeeded:', result['fileUrl'])
with open(os.environ['GITHUB_STEP_SUMMARY'], 'a') as summary:
    summary.write(f"Uploaded the existing verified 1.1.0 JAR with the full HTML changelog. No rebuild.\n\n[{jar.name}]({result['fileUrl']})\n")
