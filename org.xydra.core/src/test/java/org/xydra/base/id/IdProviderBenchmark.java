package org.xydra.base.id;

import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.runner.CaliperMain;


/**
 * The IntegerRangeIndex version is roughly 4 times faster than the regex
 * version. It also takes more memory and more objects.
 *
 * @author xamde
 *
 */
public class IdProviderBenchmark {

    static final MemoryStringIDProvider p = new MemoryStringIDProvider();

    @Param({ "CamelCase", "Innsbruck", "a1FF55EC3-1295-439B-9B43-FAF2A8A0484D", "a10.10.09",
            "HumanLanguageTechnologyForTheSemanticWeb",
            "IntegratingGeometricalAndLinguisticAnalysisForEmailSignatureBlockParsing" })
    String s;

    @Benchmark
    public boolean isValid_IntegerIntervals(final int reps) {
        final String s = this.s;
        boolean dummy = false;
        for(int i = 0; i < reps; i++) {
            dummy |= BaseStringIDProvider.isValidId(s);
        }
        return dummy;
    }

    @Benchmark
    public boolean isValid_Regexp(final int reps) {
        final String s = this.s;
        boolean dummy = false;
        for(int i = 0; i < reps; i++) {
            dummy |= BaseStringIDProvider.isValidId_versionB(s);
        }
        return dummy;
    }

    public static void main(final String[] args) {
        CaliperMain.main(IdProviderBenchmark.class, new String[] {});
    }

}
