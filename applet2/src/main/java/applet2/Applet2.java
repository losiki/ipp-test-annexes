package applet2;

import javacard.framework.AID;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Shareable;
import javacard.framework.Util;
import uicc.toolkit.*;
import uicc.usim.toolkit.ToolkitConstants;
import uicc.usim.toolkit.USATEnvelopeHandler;
import uicc.usim.toolkit.USATEnvelopeHandlerSystem;

/**
 * SIMalliance Test Toolkit Applet2
 */
public class Applet2 extends Applet
    implements ToolkitInterface {

    static final byte    GET_DATA        = (byte)0xCA;
    static final short   PROPRIETARY_TAG = (short)0x0092;
    private static short DGI_0070        = 0x0070;
    private byte[]       data;
    private byte[]       dataIn;

    /**
     * Default Applet constructor
     */
    public Applet2() {
        data = new byte[0x100];
        dataIn = new byte[0x100];
// nothing to do
    }

    /**
     * Create an instance of the applet, the Java Card runtime environment will
     * call this static method first.
     *
     * @param buffer the array containing installation parameters
     * @param offset the starting offset in buffer
     * @param length the length in bytes of the parameter data in buffer
     * @throws ISOException if the install method failed
     */
    public static void install(byte[] buffer, short offset, byte length)
        throws ISOException {
        Applet2 applet2 = new Applet2();
        byte aidLength = buffer[offset];
        if (aidLength == (byte)0) {
            applet2.register();
        } else {
            applet2.register(buffer, (short)(offset + 1), aidLength);
        }
        applet2.registerEvent();
    }
    /*
     * (non-Javadoc)
     * @see Applet#process(javacard.framework.APDU)
     */

    public void process(APDU apdu) throws ISOException {

        short outputLength = 0;

        if (this.selectingApplet()) {
            return;
        }

        byte apduBuffer[] = apdu.getBuffer();

        //To check locally that everythings works ok.
        switch (apduBuffer[ISO7816.OFFSET_INS]) {
            //Check the INS is a GET DATA command
            case GET_DATA:

                outputLength = processGetDataCommand(apduBuffer,
                    ISO7816.OFFSET_CLA);
                break;

            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }

        apdu.setOutgoing();
        apdu.setOutgoingLength((short)(outputLength - ISO7816.OFFSET_CDATA));
        apdu.sendBytes((short)ISO7816.OFFSET_CDATA,
            (short)(outputLength - ISO7816.OFFSET_CDATA));
    }
    /*
     * (non-Javadoc)
     * @see Applet#getShareableInterfaceObject(javacard.framework.AID, byte)
     */

    public Shareable getShareableInterfaceObject(AID clientAID, byte param) {
        if ((param == (byte)0x01) && (clientAID == null)) {
            return ((Shareable)this);
        }
        return null;
    }
    /*
     * (non-Javadoc)
     * @see uicc.toolkit.ToolkitInterface#processToolkit(short)
     */

    public void processToolkit(short event) throws ToolkitException {
        short response = 0;

        //Event which the application is registered
        if (event == ToolkitConstants.EVENT_FORMATTED_SMS_PP_ENV) {
            USATEnvelopeHandler envH = USATEnvelopeHandlerSystem
                .getTheHandler();
            //Copy enter data in a buffer
            envH.copyValue(envH.getSecuredDataOffset(), dataIn, (short)0,
                envH.getSecuredDataLength());

            //Obtain the lenght of the response
            response = processGetDataCommand(dataIn, ISO7816.OFFSET_CLA);

            EnvelopeResponseHandler envRespH = EnvelopeResponseHandlerSystem
                .getTheHandler();
            //Copy data
            envRespH.appendArray(dataIn, ISO7816.OFFSET_CDATA,
                (short)(response - ISO7816.OFFSET_CDATA));
            //Show data
            envRespH.post(true);
        }
    }

    /**
     * Registration to the event EVENT_FORMATTED_SMS_PP_ENV
     */
    private void registerEvent() {
        ToolkitRegistrySystem.getEntry()
            .setEvent(ToolkitConstants.EVENT_FORMATTED_SMS_PP_ENV);
    }

    public short processData(byte[] bytes, short s, short s1, byte[] bytes1,
        short s2) {

        byte offsetData = ISO7816.OFFSET_CDATA + 2;
        short dgi = Util.getShort(bytes, ISO7816.OFFSET_CDATA);

        //Check the STORE DATA got the DGI 0070
        if (dgi == DGI_0070) {
            if (bytes[offsetData] == (byte)0xFF) {
                offsetData++;
            }

            offsetData++;

            //Copy the data from our PROPRIETARY_TAG
            if (bytes[offsetData] == (byte)0x92) {
                Util.arrayCopy(bytes, offsetData, data, (short)0,
                    (short)(bytes[offsetData + 1] + 2));
                bytes1[s2] = 0;
                return 1;
            }
        }

        ISOException.throwIt(ISO7816.SW_WRONG_DATA);
        return 0;
    }

    private short processGetDataCommand(byte[] apduBuffer, short offset) {
        //Initialize length variable
        short outputDataLength = 0;

        //Check if P1 and P2 are our PROPRIETARY_TAG = 00 92
        if (Util.getShort(apduBuffer,
            (short)(offset + ISO7816.OFFSET_P1)) == PROPRIETARY_TAG) {
            if (data[0] != 0) {
                outputDataLength = Util.arrayCopy(data, (short)0x00, apduBuffer,
                    (short)(offset + ISO7816.OFFSET_CDATA),
                    (short)(data[1] + 2));
            } else {
                ISOException.throwIt((short)0x6A88);
            }
        } else {
            //If it is not our PROPRIETARY_TAG, then return an exception.
            ISOException.throwIt((short)0x6A88);
        }

        return outputDataLength;
    }
}