package com.patrones.u2;

import java.util.List;

public interface ReportBody {
    String render(List<GradeRecord> records);
}