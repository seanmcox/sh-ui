/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.batik.transcoder.image;

import java.util.HashMap;
import java.util.Map;

import org.apache.batik.transcoder.TranscoderInput;

/**
 * Test the ImageTranscoder with the KEY_PIXEL_UNIT_TO_MILLIMETER
 * transcoding hint.
 *
 * @author <a href="mailto:Thierry.Kormann@sophia.inria.fr">Thierry Kormann</a>
 * @version $Id: PixelToMMTest.java 1733420 2016-03-03 07:41:59Z gadams $ */
public class PixelToMMTest extends AbstractImageTranscoderTest {

    /** The URI of the input image. */
    protected String inputURI;

    /** The URI of the reference image. */
    protected String refImageURI;

    /** The pixel to mm factor. */
    protected Float px2mm;

    /**
     * Constructs a new <code>PixelToMMTest</code>.
     *
     * @param inputURI the URI of the input image
     * @param refImageURI the URI of the reference image
     * @param px2mm the pixel to mm conversion factor
     */
    public PixelToMMTest(String inputURI, 
                         String refImageURI, 
                         Float px2mm) {
        this.inputURI = inputURI;
        this.refImageURI = refImageURI;
        this.px2mm = px2mm;
    }

    /**
     * Creates the <code>TranscoderInput</code>.
     */
    protected TranscoderInput createTranscoderInput() {
        return new TranscoderInput(resolveURL(inputURI).toString());
    }
    
    /**
     * Creates a Map that contains additional transcoding hints.
     */
    protected Map createTranscodingHints() {
        Map hints = new HashMap(3);
        hints.put(ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, px2mm);
        return hints;
    }

    /**
     * Returns the reference image for this test.
     */
    protected byte [] getReferenceImageData() {
        return createBufferedImageData(resolveURL(refImageURI));
    }
}
