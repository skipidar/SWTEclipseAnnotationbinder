package de.ivu.fare.e4.annotations.eval;

import java.lang.reflect.Field;

import javax.inject.Inject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.widgets.Widget;

import de.ivu.fare.e4.annotations.BindWidgetSelectionToModel;
import de.ivu.fare.e4.annotations.extractor.IWidgetExtractor;

/**
 * Retrieves the Attributes of the annotation and bind to the text property of widget
 *
 * @author alf
 *
 */
class EvaluatorSelection {

    // will lookup required instances in this context
    @Inject
    IEclipseContext eclipseContext;

    @Inject
    DataBindingContext jfaceDataBindingContext;

    private EvaluatorTools evaluatorTools;


    /**
     * retrieve all attributes from TEXT-binding annotation and bind
     *
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    void bindWidgetsSelectionAttribute(Field annotatedField, Object annotatedFieldValue,
            BindWidgetSelectionToModel bindingToSelectionAnnotation) throws NoSuchFieldException, SecurityException {

        // skip if there is no such annotation
        if (bindingToSelectionAnnotation == null) {
            return;
        }

        // retrieve ClassOfModel
        Class<?> modelClass = bindingToSelectionAnnotation.modelClass();
        // retrieve FieldInModel
        String modelFieldName = bindingToSelectionAnnotation.modelFieldName();
        // retrieve WidgetRetriever
        Class<? extends IWidgetExtractor> widgetRetrieverClass = bindingToSelectionAnnotation.widgetExtractor();

        // now retrieve instances from Context
        // retrieve Widget using: annotated object, WidgetRetriever
        IWidgetExtractor widgetRetriever = EvaluatorTools.findInContextOrMake(widgetRetrieverClass, eclipseContext, false);

        // retrieve Model from the IEclipseContext
        Object model = EvaluatorTools.findInContextOrMake(modelClass, eclipseContext, false);

        // get Model's Field from AnnotationsParameter "FieldInModel" and Model
        Field modelField = EvaluatorTools.getPublicOrPrivateDeclaredField(model.getClass(), modelFieldName);

        // get the Widget which we should bind to
        Widget widget = EvaluatorTools.getWidget(widgetRetriever, annotatedFieldValue, modelFieldName, modelClass);

        bindWidgetpropertySelectionToModel(bindingToSelectionAnnotation, modelFieldName, model, widget, annotatedFieldValue);
    }

    /** do the binding between field and widget' text property */
    private void bindWidgetpropertySelectionToModel(BindWidgetSelectionToModel bindingToSelectionAnnotation,
            String modelFieldName, Object model, Widget widget, Object annotatedFieldValue) {

        EvaluatorTools.validateWidget(widget, modelFieldName);

        // bind the Model's Field to the Widget's TEXT attribute

        // Selection..
        IObservableValue observeSelectionInWidget = null;

        try {
            observeSelectionInWidget = WidgetProperties.selection().observe(widget);
        } catch (Exception e) {
            // log the exception
            new IllegalStateException(String.format(
                    "Failed to bind field '%s' to selection-events of widget with Type '%s'", modelFieldName, widget
                            .getClass().getSimpleName()), e).printStackTrace();
            return;
        }

        // Observe model's field
        IObservableValue observeModelfieldInPojo = BeanProperties.value(bindingToSelectionAnnotation.modelClass(),
                modelFieldName).observe(model);

        if (evaluatorTools == null) {
            evaluatorTools = new EvaluatorTools(eclipseContext);
        }


        // do the binding
        evaluatorTools.bindWithUpdateStrategy(jfaceDataBindingContext, annotatedFieldValue, observeSelectionInWidget, observeModelfieldInPojo);

    }
}
