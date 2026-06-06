from django.contrib import admin
from django.urls import path, include
from django.views.generic import TemplateView

urlpatterns = [
    path("", TemplateView.as_view(template_name="index.html"), name="landing"),
    path("admin/", admin.site.urls),
    path("auth/", include("accounts.urls")),
    path("", include("finance.urls")),
]
