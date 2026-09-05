package com.patrones.u2;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ReportFactoryRegistry {

    private static final Map<String, Supplier<ReportFormatFactory>> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put("pdf", PdfReportFactory::new);
        REGISTRY.put("excel", ExcelReportFactory::new);
        REGISTRY.put("html", HtmlReportFactory::new);
    }

    private ReportFactoryRegistry() {
        // Evita instanciacion accidental de una clase que solo agrupa
        // comportamiento estatico.
    }

    public static void register(String format, Supplier<ReportFormatFactory> factory) {
        REGISTRY.put(format.toLowerCase(), factory);
    }

    public static ReportFormatFactory resolve(String format) {
        Supplier<ReportFormatFactory> factory = REGISTRY.get(format.toLowerCase());
        if (factory == null) {
            throw new IllegalArgumentException(
                    "Formato de reporte no registrado: " + format +
                    ". Formatos disponibles: " + REGISTRY.keySet());
        }
        return factory.get();
    }
}