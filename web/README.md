# Studly PWA

This folder contains the installable iOS web app.

## Local run

```bash
cd web
python -m http.server 4173
```

Open `http://localhost:4173` in Safari or Chrome.

## iOS install

Deploy the `web/` folder over HTTPS, open the URL in Safari, then use Share > Add to Home Screen.

OAuth uses the current page URL as `redirect_uri` by default. If Kordis rejects that URL, register the deployed URL with the OAuth client or paste a valid access token in the login screen.
