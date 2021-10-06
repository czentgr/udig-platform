/**
 * uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Refractions BSD
 * License v1.0 (http://udig.refractions.net/files/bsd3-v10.html).
 */
package org.locationtech.udig.internal.ui;

import javax.xml.transform.TransformerException;

import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.geotools.gml.producer.GeometryTransformer;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.udig.ui.AbstractTextStrategizedTransfer;

public class GeometryTextTransfer extends AbstractTextStrategizedTransfer implements UDIGTransfer {

    private static GeometryTextTransfer _instance = new GeometryTextTransfer();

    private GeometryTextTransfer() {

    }

    /**
     * Returns the singleton instance of the TextTransfer class.
     *
     * @return the singleton instance of the TextTransfer class
     */
    public static GeometryTextTransfer getInstance() {
        return _instance;
    }

    private TransferStrategy[] transferStrategies;

    @Override
    public synchronized TransferStrategy[] getAllStrategies() {
        if (transferStrategies == null) {
            transferStrategies = new TransferStrategy[] { new JtsWktStrategy() };
        }

        TransferStrategy[] copy = new TransferStrategy[transferStrategies.length];
        System.arraycopy(transferStrategies, 0, copy, 0, transferStrategies.length);
        return copy;
    }

    @Override
    public String[] getStrategyNames() {
        return new String[] { "JTS WKT" }; //$NON-NLS-1$
    }

    @Override
    public String getTransferName() {
        return "Geometry"; //$NON-NLS-1$
    }

    @Override
    public TransferStrategy getDefaultStrategy() {
        return getAllStrategies()[0];
    }

    @Override
    public TransferData[] getSupportedTypes() {
        return TextTransfer.getInstance().getSupportedTypes();
    }

    @Override
    public boolean isSupportedType(TransferData transferData) {
        return TextTransfer.getInstance().isSupportedType(transferData);
    }

    @Override
    public boolean validate(Object object) {
        return object instanceof Geometry;
    }

    /**
     * This strategy exports geometries as Well Known Text as generated by JTS WKTWriter.
     *
     * @author jeichar
     */
    public static class JtsWktStrategy implements TransferStrategy {

        /**
         * @see Transfer#javaToNative
         */
        @Override
        public void javaToNative(Object object, TransferData transferData) {
            String stringToEncode;

            Geometry feature = (Geometry) object;

            WKTWriter writer = new WKTWriter();
            String geometry = writer.writeFormatted(feature);

            stringToEncode = geometry;
            TextTransfer.getInstance().javaToNative(stringToEncode, transferData);
        }

        /**
         * @see Transfer#nativeToJava
         */
        @Override
        public Object nativeToJava(TransferData transferData) {
            String string = (String) TextTransfer.getInstance().nativeToJava(transferData);

            WKTReader reader = new WKTReader();
            try {
                return reader.read(string);
            } catch (ParseException e) {
                // JONES
            }
            return null;
        }

    }

    /**
     * This strategy encodes geometries as GML Geometries.
     *
     * @author jeichar
     */
    public static class GMLStrategy implements TransferStrategy {

        /**
         * @see Transfer#javaToNative
         */
        @Override
        public void javaToNative(Object object, TransferData transferData) {
            Geometry geometry = (Geometry) object;
            GeometryTransformer transformer = new GeometryTransformer();

            transformer.setIndentation(2);

            try {
                TextTransfer.getInstance().javaToNative(transformer.transform(geometry),
                        transferData);
            } catch (TransformerException e) {
                throw (RuntimeException) new RuntimeException().initCause(e);
            }
        }

        /**
         * @see Transfer#nativeToJava
         */
        @Override
        public Object nativeToJava(TransferData transferData) {
            return null;
        }

    }

}
