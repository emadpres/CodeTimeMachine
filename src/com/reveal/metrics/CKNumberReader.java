package com.reveal.metrics;


import com.github.mauricioaniche.ck.CKNumber;
import org.omg.CORBA.INV_FLAG;

public class CKNumberReader
{

    private final int INVALID = -1;

    // Rearrange enum to change the order of display.
    // Remember to keep `MetricTypes_StringRepresntation` variable in sync.
    public static enum MetricTypes
    {loc,wmc, dit, noc, cbo, lcom, rfc, nom, nopm, nosm, nof, nopf, nosf, nosi,NONE /*update getValueForMetric(..) after adding new item here*/};

    // Keep in sync with above enum
    public static String[] MetricTypes_StringRepresntation =
            {
                    "Line of Code",/*LOC: It counts the lines of count, ignoring empty lines.*/
                    "Class Cyclomatic Complexity",/*WMC: Weight Method Class or McCabe's complexity. It counts the number of branch instructions in a class.*/
                    "Depth of Inheritance Tree",/*DIT: It counts the number of "fathers" a class has. All classes have DIT at least 1 (everyone inherits java.lang.Object). In order to make it happen, classes must exist in the project (i.e. if a class depends upon X which relies in a jar/dependency file, and X depends upon other classes, DIT is counted as 2).*/
                    "Number of Children", /*NOC: Counts the number of children a class has.*/
                    "Coupling Between Objects", /*CBO: Counts the number of dependencies a class has. The tools checks for any type used in the entire class (field declaration, method return types, variable declarations, etc). It ignores dependencies to Java itself (e.g. java.lang.String)*/
                    "Lack of Cohesion of Methods", /*LCOM: This is the very first version of metric, which is not reliable. LCOM-HS can be better (hopefully, you will send us a pull request)*/
                    "Response for a Class",/*RFC: Counts the number of unique method invocations in a class. As invocations are resolved via static analysis, this implementation fails when a method has overloads with same number of parameters, but different types.*/
                    "Number of Methods",
                    "Number of Public Methods",
                    "Number of Static Methods",
                    "Number of Fields",
                    "Number of Public Fields",
                    "Number of Static Fields",
                    "Number of Static Invocations", /*NOSI: Counts the number of invocations to static methods. It can only count the ones that can be resolved by the JDT*/
                    ""
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
