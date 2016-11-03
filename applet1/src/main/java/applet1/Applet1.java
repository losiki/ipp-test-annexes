package applet1;

import javacard.framework.AID;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISOException;
import javacard.framework.Shareable;
import uicc.toolkit.ToolkitException;
import uicc.toolkit.ToolkitInterface;
import uicc.toolkit.ToolkitRegistrySystem;
import uicc.usim.toolkit.ToolkitConstants;

/**
 * SIMalliance Test Toolkit Applet1
 */
public class Applet1 extends Applet implements ToolkitInterface {

    /**
     * Constructor
     */
    public Applet1() {
        super();
    }

    /**
     * @param buffer the array containing installation parameters.
     * @param offset the starting offset in buffer.
     * @param length the length in bytes of the parameter data in buffer. The
     *            maximum value of length is 127.
     * @throws ISOException if the install method failed.
     */
    public static void install(byte[] buffer, short offset, byte length)
        throws ISOException {
        Applet1 applet1 = new Applet1();
        byte aidLength = buffer[offset];
        if (aidLength == (byte)0) {
            // This method is used by the applet to register this applet instance with the Java Card runtime environment and to assign the Java Card platform name of the applet as its instance AID bytes.
            applet1.register();
        } else {
            // This method is used by the applet to register this applet instance with the Java Card runtime environment and assign the specified AID bytes as its instance AID bytes.
            applet1.register(buffer, (short)(offset + 1), aidLength);
        }
        applet1.registerEvent();
    }

    /**
     * @param apdu the incoming APDU object
     */
    public void process(APDU apdu) throws ISOException {
        // nothing to do
    }

    /**
     * Parameters:
     *
     * @param clientAID the AID object of the client applet
     * @param parameter The optional parameter byte. The parameter byte may be
     *            used by the client to specify which shareable interface object
     *            is being requested.
     * @return The shareable interface object or null
     */
    public Shareable getShareableInterfaceObject(AID clientAID,
        byte parameter) {
        if ((parameter == (byte)0x01) && (clientAID == null)) {
            return ((Shareable)this);
        }
        return null;
    }

    /**
     * This method is the standard toolkit event handling method of a toolkit
     * applet and is called by the "Triggering Entity" to process the current
     * Toolkit event.
     * This method is invoked for notification of registered events.
     *
     * @param event the type of event to be processed.
     */
    public void processToolkit(short event) throws ToolkitException {
        // nothing to do
    }

    /**
     * Registration to the event EVENT_FORMATTED_SMS_PP_ENV
     */
    private void registerEvent() {
        ToolkitRegistrySystem.getEntry()
            .setEvent(ToolkitConstants.EVENT_FORMATTED_SMS_PP_ENV);
    }

}
