package xyqb.net.resultfilter;

import xyqb.library.XmlElement;
import xyqb.library.XmlHelper;

/**
 * Created by cz on 8/23/16.
 */
public class XmlDomResultFilter extends ResultFilter {
    @Override
    public XmlElement result(String result) {
        return XmlHelper.parserText(result);
    }
}
