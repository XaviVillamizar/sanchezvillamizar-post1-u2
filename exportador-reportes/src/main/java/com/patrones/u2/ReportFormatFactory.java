package com.patrones.u2;

public interface ReportFormatFactory {
    ReportBody createBody();
    ReportHeaderFooter createHeaderFooter();
}