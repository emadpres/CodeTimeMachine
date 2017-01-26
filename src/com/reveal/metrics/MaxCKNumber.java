package com.reveal.metrics;


import com.github.mauricioaniche.ck.CKNumber;

public class MaxCKNumber extends CKNumber
{
    public MaxCKNumber(String file, String className, String type)
    {
        super(file, className, type);
    }

    public void updateMaxIfNeeded(CKNumber newCKNumber)
    {
        // CKNumber = result of all metrics for a file
        if(dit < newCKNumber.getDit())
            dit = newCKNumber.getDit();

        if(noc < newCKNumber.getNoc())
            noc = newCKNumber.getNoc();

        if(wmc < newCKNumber.getWmc())
            wmc = newCKNumber.getWmc();

        if(cbo < newCKNumber.getCbo())
            cbo = newCKNumber.getCbo();

        if(lcom < newCKNumber.getLcom())
            lcom = newCKNumber.getLcom();

        if(rfc < newCKNumber.getRfc())
            rfc = newCKNumber.getRfc();

        if(nom < newCKNumber.getNom())
            nom = newCKNumber.getNom();

        if(nopm < newCKNumber.getNopm())
            nopm = newCKNumber.getNopm();

        if(nosm < newCKNumber.getNosm())
            nosm = newCKNumber.getNosm();

        if(nof < newCKNumber.getNof())
            nof = newCKNumber.getNof();

        if(nopf < newCKNumber.getNopf())
            nopf = newCKNumber.getNopf();

        if(nosf < newCKNumber.getNosf())
            nosf = newCKNumber.getNosf();

        if(nosi < newCKNumber.getNosi())
            nosi = newCKNumber.getNosi();

        if(loc < newCKNumber.getLoc())
            loc = newCKNumber.getLoc();

    }
}
