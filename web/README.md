# Studly PWA

This folder contains the installable iOS web app.

## Local run

```bash
cd web
python -m http.server 4173
```

Open `http://localhost:4173` in Safari or Chrome.

## Docker

```bash
docker compose up --build
```

Open `http://localhost:8087`.

## iOS install

Deploy the `web/` folder over HTTPS, open the URL in Safari, then use Share > Add to Home Screen.

OAuth only sends `redirect_uri` when one is explicitly configured. The URL must be allowlisted by Kordis for the `skolae-app` OAuth client; otherwise Kordis returns 403. For local testing, leave it empty and paste an access token manually, or register the deployed HTTPS URL with the OAuth client.
