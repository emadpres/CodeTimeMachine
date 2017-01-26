package com.reveal.metrics;


import com.github.mauricioaniche.ck.CKNumber;
import org.omg.CORBA.INV_FLAG;

public class CKNumberReader
{

    private final int INVALID = -1;

    public static enum MetricTypes
    {NONE, dit, noc, wmc, cbo, lcom, rfc, nom, nopm, nosm, nof, nopf, nosf, nosi, loc /*update getValueForMetric(..) after addding new item here*/};

    public static String[] MetricTypes_StringRepresntation =
            {
                    "",
                    "Depth Inheritance Tree",
                    "Number of Children",
                    "Weight Method Class",
                    "Coupling between objects",
                    "Lack of Cohesion of Methods",
                    "Response for a Class",
                    "Number of methods",
                    "Number of public methods",
                    "Number of static methods",
                    "Number of fields",
                    "Number of public fields",
                    "Number of static fields",
                    "Number of static invocations",
                    "Line of code"
            };

    static CKNumberReader instance = new CKNumberReader();

    private CKNumberReader()
    {
    }

    static public CKNumberReader getInstance()
    {
        return instance;
    }

    public int getValueForMetric(CKNumber metricsResult, CKNumberReader.MetricTypes metric)
    {
        switch (metric)
        {
            case NONE:
                return INVALID;
            case dit:
                return metricsResult.getDit();
            case noc:
                return metricsResult.getNoc();
            case wmc:
                return metricsResult.getWmc();
            case cbo:
                return metricsResult.getCbo();
            case lcom:
                return metricsResult.getLcom();
            case rfc:
                return metricsResult.getRfc();
            case nom:
                return metricsResult.getNom();
            case nopm:
                return metricsResult.getNopm();
            case nosm:
                return metricsResult.getNosm();
            case nof:
                return metricsResult.getNof();
            case nopf:
                return metricsResult.getNopf();
            case nosf:
                return metricsResult.getNosf();
            case nosi:
                return metricsResult.getNosi();
            case loc:
                return metricsResult.getLoc();
        }
        return INVALID;
    }

}
