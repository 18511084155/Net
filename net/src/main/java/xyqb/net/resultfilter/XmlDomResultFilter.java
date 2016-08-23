package xyqb.net.resultfilter;

import xyqb.xml.XmlElement;
import xyqb.xml.XmlHelper;

/**
 * Created by cz on 8/23/16.
 */
public class XmlDomResultFilter extends ResultFilter<XmlElement> {
    @Override
    public XmlElement result(String result) {
        return XmlHelper.parserText(result);
    }
}
