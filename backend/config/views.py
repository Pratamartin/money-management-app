import urllib.request
import urllib.error
import json

from django.http import StreamingHttpResponse, HttpResponse


GITHUB_RELEASES_API = (
    "https://api.github.com/repos/Pratamartin/money-management-app/releases/latest"
)


def download_apk(request):
    try:
        req = urllib.request.Request(
            GITHUB_RELEASES_API,
            headers={"User-Agent": "MoneyMgtApp-Server", "Accept": "application/vnd.github+json"},
        )
        with urllib.request.urlopen(req, timeout=10) as resp:
            release = json.loads(resp.read())

        apk_asset = next(
            (a for a in release.get("assets", []) if a["name"].endswith(".apk")),
            None,
        )
        if not apk_asset:
            return HttpResponse("APK não encontrado no release mais recente.", status=404)

        apk_url = apk_asset["browser_download_url"]
        filename = apk_asset["name"]

        apk_req = urllib.request.Request(
            apk_url,
            headers={"User-Agent": "MoneyMgtApp-Server"},
        )
        apk_resp = urllib.request.urlopen(apk_req, timeout=60)

        response = StreamingHttpResponse(
            _stream(apk_resp),
            content_type="application/vnd.android.package-archive",
        )
        response["Content-Disposition"] = f'attachment; filename="{filename}"'
        content_length = apk_resp.headers.get("Content-Length")
        if content_length:
            response["Content-Length"] = content_length
        return response

    except urllib.error.URLError as e:
        return HttpResponse(f"Erro ao buscar o APK: {e}", status=502)


def _stream(response, chunk=65536):
    try:
        while True:
            data = response.read(chunk)
            if not data:
                break
            yield data
    finally:
        response.close()
