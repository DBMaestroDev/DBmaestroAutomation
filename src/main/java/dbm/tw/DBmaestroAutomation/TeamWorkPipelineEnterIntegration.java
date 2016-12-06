package dbm.tw.DBmaestroAutomation;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class TeamWorkPipelineEnterIntegration extends Builder {



    //From config.jelly
    private final String pipelineName;
    private final String environmentName;
    private final boolean ignoreCheckedOutObjects;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public TeamWorkPipelineEnterIntegration(String pipelineName, String environmentName, boolean ignoreCheckedOutObjects) {
        this.pipelineName = pipelineName;
        this.environmentName = environmentName;
        this.ignoreCheckedOutObjects = ignoreCheckedOutObjects;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getPipelineName() {
        return pipelineName;
    }
    public String getEnvironmentName() {
        return environmentName;
    }
    public boolean getIgnoreCheckedOutObjects() {
        return ignoreCheckedOutObjects;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        try{
            String dbType = CommonFunctions.getDbTypeFromConfiguration(build);
            String twServer = CommonFunctions.getControllerHostFromConfiguration(build, dbType);

            ArgumentListBuilder args = new ArgumentListBuilder();
            if (!launcher.isUnix()) {
                args.add("cmd.exe");
                args.add("/C");
            }
            args.add("java");
            args.add("-jar");
            args = CommonFunctions.AddQuoted(args, CommonFunctions.getJarPathFromConfiguration(build));
            args.add("-EnterIntegration");
            args.add("-Pipeline");
            args = CommonFunctions.AddQuoted(args, pipelineName);
            args.add("-Environment");
            args = CommonFunctions.AddQuoted(args, environmentName);
            args.add("-IgnoreCheckedOutObjects");
            args = CommonFunctions.AddQuoted(args, (ignoreCheckedOutObjects ? "true" : "false"));
            args.add("-twServer");
            args = CommonFunctions.AddQuoted(args, twServer);
            args.add("-db");
            args = CommonFunctions.AddQuoted(args, dbType);
            listener.getLogger().println("Prepared command line, " + args.toString());
            String result = CommonFunctions.DoRuntime(build, launcher, listener, args);
            listener.getLogger().println("result from command line: " + result);
        }catch(Exception ex){
            listener.getLogger().println("unexpected exception has occurred: "+ex.toString());
            return false;
        }
        return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }


    /**
     * Descriptor for {@link dbm.tw.DBmaestroAutomation.TeamWorkPipelineEnterIntegration}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "DBmaestro detailed step: Enter deployment mode";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return CommonFunctions.getdbmaestroPluginDescriptor().getUseAdvancedMode();
        }
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            req.bindJSON(this, formData);
            save();
            return super.configure(req,formData);
        }
    }
}

