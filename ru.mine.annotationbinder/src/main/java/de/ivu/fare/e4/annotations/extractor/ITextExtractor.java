package de.ivu.fare.e4.annotations.extractor;

import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * Class which is able to extract a Text Widget from the given object.
 * Is used locally (e.g. in field annotations to convert fields to {@link Widget}), where it is
 * known which kind of Object is passed to it,
 * 
 * @author alf
 *
 */
public interface ITextExtractor {
    public Text getText(Object object);
}
