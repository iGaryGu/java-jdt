package astparser.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SampleHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public SampleHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String path = "/home/garygu/file/filedata";
		try {
			importproject(path);

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public void importproject(String path) throws CoreException, IOException{
		File file = new File(path);
		File[] files = file.listFiles();
		for(File f : files){
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

			IProject project = root.getProject(f.getName());
			project.create(null);
			project.open(null);
			 
			//set the Java nature
			IProjectDescription description = project.getDescription();
			description.setNatureIds(new String[] { JavaCore.NATURE_ID });
			 
			//create the project
			project.setDescription(description, null);
			IJavaProject javaProject = JavaCore.create(project);
			 
			//set the build path
			IClasspathEntry[] buildPath = {
					JavaCore.newSourceEntry(project.getFullPath().append("src")),
							JavaRuntime.getDefaultJREContainerEntry() };
			 
			javaProject.setRawClasspath(buildPath, project.getFullPath().append(
							"bin"), null);
			 
			//create folder by using resources package
			IFolder folder = project.getFolder("src");
			folder.create(true, true, null);
			 
			//Add folder to Java element
			IPackageFragmentRoot srcFolder = javaProject
							.getPackageFragmentRoot(folder);
			 

			String pathname = f.getAbsolutePath();
			deepfindfile(pathname,srcFolder,"");
			
		}
	}
	
	public void deepfindfile(String path,IPackageFragmentRoot srcfolder,String packagename) throws IOException, JavaModelException{
		File file = new File(path);

		File[] files = file.listFiles();
		for(File f : files){
			String pathname = f.getAbsolutePath();
			if(f.isFile()){
				//System.out.println(f.getName());
				StringBuilder fileData = new StringBuilder(1000);
				BufferedReader reader = new BufferedReader(new FileReader(f.getAbsoluteFile()));
				String tempstring;
				char[] buf = new char[10];
				int numRead = 0;
				while ((numRead = reader.read(buf)) != -1) {
					String readData = String.valueOf(buf, 0, numRead);
					fileData.append(readData);
					buf = new char[1024];
				}
				reader.close();
				if(packagename.length()-1<0){
					tempstring = "";
				}
				else{
					tempstring = packagename.substring(0, packagename.length()-1);
				}
				//create package fragment
				
				IPackageFragment fragment = srcfolder.createPackageFragment(tempstring, true, null);
				ICompilationUnit cu = fragment.createCompilationUnit(f.getName(), fileData.toString(),false, null); 	
			}
			else{
				//System.out.println(f.getName());
				packagename = packagename+f.getName()+".";
				deepfindfile(pathname,srcfolder,packagename);
				if(packagename.length()-f.getName().length()-1<0){
					packagename = "";
				}
				else{
					packagename = packagename.substring(0, packagename.length()-f.getName().length()-1);
				}
			}
		}
	}
}
