package com.imagem.gwtpplugin.wizard;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.imagem.gwtpplugin.project.SourceEditor;
import com.imagem.gwtpplugin.projectfile.src.client.event.Event;
import com.imagem.gwtpplugin.projectfile.src.client.event.Handler;
import com.imagem.gwtpplugin.projectfile.src.client.event.HasHandlers;

public class EventWizard extends Wizard implements INewWizard {

	private EventWizardPage eventPage;
	private IStructuredSelection selection;
	private IProject project;
	private IPath basePath;
	
	public EventWizard() {
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle("New Event");
	}
	
	@Override
	public void addPages() {
		eventPage = new EventWizardPage(selection);
		addPage(eventPage);
	}
	
	@Override
	public boolean performFinish() {
		String name = eventPage.getEventName();
		String eventPackage = eventPage.getEventPackage();
		String[] parameters = eventPage.getParameters();
		boolean hasHandlers = eventPage.hasHandlers();
		
		final Event event = new Event(name, eventPackage);
		final Handler handler = new Handler(name, eventPackage);
		final HasHandlers hasHandler = new HasHandlers(name, eventPackage);
		
		event.setParameters(SourceEditor.getVariables(project, basePath, parameters));
		
		try {
			SourceEditor.createProjectFile(project, event, true);
			SourceEditor.createProjectFile(project, handler, true);
			if(hasHandlers)
				SourceEditor.createProjectFile(project, hasHandler, true);
		} 
		catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		if(selection != null && !selection.isEmpty()) {
			if (selection.size() > 1)
				return;
			Object firstElement = selection.getFirstElement();
			IResource resource = null;
			if (firstElement instanceof IResource) {
				// Is it a IResource ?
				resource = (IResource) firstElement;
			}
			else if (firstElement instanceof IAdaptable) {
				// Is it a IResource adaptable ?
				IAdaptable adaptable = (IAdaptable) firstElement;
				resource = (IResource) adaptable.getAdapter(IResource.class);
			}
			project = resource.getProject();
			basePath = SourceEditor.getBasePath(resource.getFullPath());
		}
	}

}
