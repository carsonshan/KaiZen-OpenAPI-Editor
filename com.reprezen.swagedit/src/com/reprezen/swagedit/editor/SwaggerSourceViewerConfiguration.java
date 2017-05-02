package com.reprezen.swagedit.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
import org.eclipse.jface.text.source.ISourceViewer;

import com.reprezen.swagedit.assist.SwaggerContentAssistProcessor;
import com.reprezen.swagedit.common.editor.JsonSourceViewerConfiguration;
import com.reprezen.swagedit.core.assist.JsonContentAssistProcessor;
import com.reprezen.swagedit.editor.hyperlinks.DefinitionHyperlinkDetector;
import com.reprezen.swagedit.editor.hyperlinks.PathParamHyperlinkDetector;
import com.reprezen.swagedit.editor.hyperlinks.SwaggerReferenceHyperlinkDetector;

public class SwaggerSourceViewerConfiguration extends JsonSourceViewerConfiguration {

	public SwaggerSourceViewerConfiguration(IPreferenceStore store) {
		super(store);
	}

	@Override
	protected JsonContentAssistProcessor createContentAssistProcessor(ContentAssistant ca) {
		return new SwaggerContentAssistProcessor(ca);
	}
	
	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		return new IHyperlinkDetector[] { new URLHyperlinkDetector(), new SwaggerReferenceHyperlinkDetector(),
				new PathParamHyperlinkDetector(), new DefinitionHyperlinkDetector() };
	}

}
