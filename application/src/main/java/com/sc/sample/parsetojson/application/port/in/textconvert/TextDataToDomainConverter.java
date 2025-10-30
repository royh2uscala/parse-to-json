package com.sc.sample.parsetojson.application.port.in.textconvert;

import com.sc.sample.parsetojson.model.DateFieldMappingDef;
import java.util.List;

public interface TextDataToDomainConverter<DOMAIN extends DateFieldMappingDef> {

    List<DOMAIN> convertDataTextLines(List<String> textLines) throws FailedTextToDomainConversionException ;
}
