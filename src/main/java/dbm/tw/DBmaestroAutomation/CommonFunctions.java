package dbm.tw.DBmaestroAutomation;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.Proc;
import hudson.Util;
import hudson.model.*;
import hudson.model.Cause.UpstreamCause;
import hudson.model.Cause.UserIdCause;
import hudson.util.ArgumentListBuilder;
import hudson.util.VariableResolver;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class CommonFunctions {
    private static final Pattern WIN_ENV_VAR_REGEX = Pattern.compile("%([a-zA-Z0-9_]+)%");
    private static final Pattern UNIX_ENV_VAR_REGEX = Pattern.compile("\\$([a-zA-Z0-9_]+)");
    public static final String UNIX_SEP = "/";
    public static final String WINDOWS_SEP = "\\";
    public static final String DEFAULT_ENCODING = "UTF-8";

    public static TeamWorkJobProperty GetBuildConfiguration(AbstractBuild<?, ?> Build) throws Exception {
        Job job = Build.getParent();
        TeamWorkJobProperty BuildConfiguration = (TeamWorkJobProperty) job.getProperty(TeamWorkJobProperty.class);
        if (BuildConfiguration == null) {
            throw new Exception("BuildConfiguration is null");
        }
        return BuildConfiguration;
    }

    public static String getDbTypeFromConfiguration(AbstractBuild<?, ?> Build) throws Exception {
        TeamWorkConfiguration.pluginDescriptorImpl globalConfig = getdbmaestroPluginDescriptor();
        TeamWorkJobProperty jobConfig = GetBuildConfiguration(Build);
        if (jobConfig.getDbType() == null) {
            throw new Exception("DbType is null");
        }
        if (jobConfig.getDbType().isEmpty() || jobConfig.getDbType().equals("Default")) {
            return globalConfig.getDefaultDbType();
        } else {
            return jobConfig.getDbType();
        }
    }

    public static String getControllerHostFromConfiguration(AbstractBuild<?, ?> Build, String dbType) throws Exception {
        TeamWorkConfiguration.pluginDescriptorImpl globalConfig = getdbmaestroPluginDescriptor();
        TeamWorkJobProperty jobConfig = GetBuildConfiguration(Build);
        if (jobConfig.getControllerHost().isEmpty()) {
            if (dbType.trim().toLowerCase().equals("oracle")) {
                return globalConfig.getOracleControllerHost();
            } else {
                return globalConfig.getSqlControllerHost();
            }
        } else {
            return jobConfig.getControllerHost();
        }
    }

    public static String getJarPathFromConfiguration(AbstractBuild<?, ?> Build) throws Exception {
        TeamWorkConfiguration.pluginDescriptorImpl globalConfig = getdbmaestroPluginDescriptor();
        TeamWorkJobProperty jobConfig = GetBuildConfiguration(Build);
        if (jobConfig.getJarPath().isEmpty()) {
                return globalConfig.getClientRunPath();
        } else {
            return jobConfig.getJarPath();
        }
    }

    public static TeamWorkConfiguration.pluginDescriptorImpl getdbmaestroPluginDescriptor() {
        return (TeamWorkConfiguration.pluginDescriptorImpl) Jenkins.getInstance().getDescriptor(TeamWorkConfiguration.class);
    }

    public static String DoRuntime(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener,
                                   ArgumentListBuilder args) throws Exception {

        Authentication auth = authenticate(build);
        EnvVars env = build.getEnvironment(listener);
        if (auth != Jenkins.ANONYMOUS) {
            listener.getLogger().print("Job build initiated by:" + auth.getName() + " \n");
            listener.getLogger().flush();
        } else {
            listener.getLogger().print("Job build initiated by: Anonymous\n");
            listener.getLogger().flush();
        }
        env.putAll(build.getBuildVariables());

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamBuildListener sbl = new StreamBuildListener(baos);

            final Proc child = launcher.decorateFor(build.getBuiltOn()).launch().cmds(args)
                    .envs(env).stdout(sbl).pwd(build.getWorkspace()).start();

            final ProcessResultReader stderr = new ProcessResultReader(child.getStderr(), "STDERR");
            stderr.start();
            while (child.isAlive()) {
                baos.flush();
                String s = baos.toString();
                baos.reset();

                listener.getLogger().print(s);
                listener.getLogger().flush();
                Thread.sleep(2);
            }
            baos.flush();
            listener.getLogger().print(baos.toString());
            listener.getLogger().flush();
            final int exitValue = child.join();

            if (exitValue == 0) {
                return "";
            } else {
                throw new Exception(stderr.toString());
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static String convertSeparator(String cmdLine, String newSeparator) {
        String match = "[/" + Pattern.quote("\\") + "]";
        String replacement = Matcher.quoteReplacement(newSeparator);

        Pattern words = Pattern.compile("\\S+");
        Pattern urls = Pattern.compile("(https*|ftp|git):");
        StringBuffer sb = new StringBuffer();
        Matcher m = words.matcher(cmdLine);
        while (m.find()) {
            String item = m.group();
            if (!urls.matcher(item).find()) {                
                m.appendReplacement(sb, Matcher.quoteReplacement(item.replaceAll(match, replacement)));
            }
        }
        m.appendTail(sb);

        return sb.toString();
    }

    public static String ReplaceEnvironmentVariables(String toResolve, final AbstractBuild<?, ?> build, BuildListener listener) throws Exception {
        if (toResolve == null) {
            return null;
        }

        VariableResolver<String> vr = build.getBuildVariableResolver();
        String resolved = Util.replaceMacro(toResolve, vr);
        try {
            EnvVars env = build.getEnvironment(listener);
            resolved = env.expand(resolved);
        } catch (Exception ex) {

        }
        resolved = envVar(resolved);
        return resolved;
    }

    public static String envVar(final String toResolve) {
        return Util.replaceMacro(toResolve, System.getenv());
    }

    private static UserIdCause getRootUserIdCause(final AbstractBuild<?, ?> build) {
        Run<?, ?> upstream = null;
        for (Cause c : build.getCauses()) {
            if (c instanceof UserIdCause) {
                return (UserIdCause) c;
            } else if (c instanceof UpstreamCause) {
                upstream = ((UpstreamCause) c).getUpstreamRun();
            }
        }

        while (upstream != null) {
            UserIdCause cause = upstream.getCause(UserIdCause.class);
            if (cause != null) {
                return cause;
            }

            UpstreamCause upstreamCause = upstream.getCause(UpstreamCause.class);
            upstream = (upstreamCause != null) ? upstreamCause.getUpstreamRun() : null;
        }

        return null;
    }

    private static Authentication authenticate(final AbstractBuild build) {
        Cause.UserIdCause cause = getRootUserIdCause(build);
        if (cause != null) {
            User u = User.get(cause.getUserId(), false, Collections.emptyMap());
            if (u == null) {
                return Jenkins.ANONYMOUS;
            }
            return u.impersonate();
        }

        return Jenkins.ANONYMOUS;
    }

    /**
     * Looks up the actual credentials.
     *
     * @param build the build.
     * @return the credentials
     */
    protected static StandardUsernamePasswordCredentials getCredentials(AbstractBuild<?, ?> build) throws Exception {
        TeamWorkJobProperty jobConfig = GetBuildConfiguration(build);
        IdCredentials cred = CredentialsProvider.findCredentialById(jobConfig.getCredentialsId(), IdCredentials.class, build);
        if (cred == null)
            return null;

        if (cred instanceof StandardUsernamePasswordCredentials)
            return (StandardUsernamePasswordCredentials) cred;

        Descriptor expected = Jenkins.getInstance().getDescriptor(cred.getClass());
        throw new Exception("Credentials '" + jobConfig.getCredentialsId() + "' is of type '" +
                cred.getDescriptor().getDisplayName() + "' where '" +
                (expected != null ? expected.getDisplayName() : cred.getClass().getName()) +
                "' was expected");
    }

    public static ArgumentListBuilder AddQuoted(ArgumentListBuilder args, String value){

        if(jenkins.model.Jenkins.VERSION.startsWith("1") || value.isEmpty())
        {
            args.addQuoted(value);
        }else{
            args.add(value);
        }
        return args;
    }
}
