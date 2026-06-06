from django.contrib import admin
from django.urls import path, include
from django.views.generic import TemplateView

from config.views import download_apk

urlpatterns = [
    path("", TemplateView.as_view(template_name="index.html"), name="landing"),
    path("download/apk", download_apk, name="download_apk"),
    path("admin/", admin.site.urls),
    path("auth/", include("accounts.urls")),
    path("", include("finance.urls")),
]
