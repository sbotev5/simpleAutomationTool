import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputAdapter;

import java.awt.*;

public class MouseListener extends NativeMouseInputAdapter {


    public void nativeMouseReleased(NativeMouseEvent e) {

        if (Menu.shouldRecord) {

            Menu.userMovements.put("MouseButton" + Menu.diffMousePress, e.getButton());
            Menu.diffMousePress++;

        }
    }

    public void nativeMouseMoved(NativeMouseEvent e) {

        if (Menu.shouldRecord) {

            Menu.userMovements.put("MouseMove" + Menu.diffMouseMove, new Point(e.getX(), e.getY()));
            Menu.diffMouseMove++;

        }

    }

    public void nativeMouseDragged(NativeMouseEvent e) {

        if (Menu.shouldRecord) {

            Menu.userMovements.put("MouseDrag" + Menu.diffMouseMove, new Point(e.getX(), e.getY()));
            Menu.diffMouseMove++;

        }
    }

}