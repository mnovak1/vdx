import java.lang.reflect.Method;

/**
 * Created by mnovak on 10/24/16.
 */
public abstract class AbstractServer implements Server {

    /**
     *
     * @return returns search calling stackstrace for @ServerConfig annotation and returns it, returns null if there is none
     */
    static ServerConfig getServerConfig() {

        Throwable t = new Throwable();
        StackTraceElement[] elements = t.getStackTrace();
        String callerMethodName = null;
        String callerClassName = null;
        ServerConfig serverConfig = null;

        for (int level = 1; level < elements.length; level++) {
            try {
                callerClassName = elements[level].getClassName();
                callerMethodName = elements[level].getMethodName();
                Method method = Class.forName(callerClassName).getMethod(callerMethodName);
                serverConfig = method.getAnnotation(ServerConfig.class);
                if (serverConfig != null)   {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return serverConfig;
    }
}
