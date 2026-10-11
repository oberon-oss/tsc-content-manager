package eu.oberon.oss.tsc.cm.cli;

import eu.oberonoss.tsc.cm.base.DataFormat;
import eu.oberonoss.tsc.cm.base.SERZData;
import eu.oberonoss.tsc.cm.base.SERZDataFlavour;

public class ReadBinarySerzFile implements SERZData {

    @Override
    public DataFormat getFormat() {
        return DataFormat.BINARY;
    }

    @Override
    public SERZDataFlavour getFlavour() {
        return SERZDataFlavour.SERZ_SCENARIO;
    }
}
