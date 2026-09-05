package com.patrones.u2;

public interface ReportHeaderFooter {
    String renderHeader(String institutionName);
    String renderFooter(int pageNumber);
}