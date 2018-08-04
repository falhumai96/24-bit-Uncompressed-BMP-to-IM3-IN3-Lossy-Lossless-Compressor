
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFileChooser;

public class BMPViewer extends javax.swing.JDialog {

    private final boolean isBMPMode;
    private final boolean isLossy;
    private byte[] compressed;

    // Constructor for the viewer class.
    public BMPViewer(java.awt.Frame parent, boolean modal, byte[] bytes, String fileName, boolean isBMPMode, boolean isLossless) {
        super(parent, modal);
        this.isLossy = isLossless;
        this.isBMPMode = isBMPMode;
        imageBytes = bytes.clone();
        initComponents();
        setTitle(String.format("BMP Operations on \"%s\"", fileName));
        setupImages();
        getPrev();
        renderPanel(getNext());
    }

    // Convert 24 bit image to BufferedImage.
    private BufferedImage createImageFromBytes(byte[] imageData) {
        int bitmapOffset = getInt(imageData[13], imageData[12], imageData[11], imageData[10]);
        int width = getInt(imageData[21], imageData[20], imageData[19], imageData[18]);
        int height = getInt(imageData[25], imageData[24], imageData[23], imageData[22]);

        int colorsUsed = getInt(imageData[49], imageData[48], imageData[47], imageData[46]);
        short bitsPerPixel = getShort(imageData[29], imageData[28]);

        if (colorsUsed == 0) {
            if (bitsPerPixel < 16) {
                colorsUsed = 1 << bitsPerPixel;
            } else {
                colorsUsed = 0;
            }
        }

        // This is where color palets and data starts.
        int current = 50;

        // Collect the color pallet.
        byte[] r, g, b;
        if (colorsUsed > 0) {
            r = new byte[colorsUsed];
            g = new byte[colorsUsed];
            b = new byte[colorsUsed];

            for (int i = 0; i < colorsUsed; i++) {
                b[i] = imageData[current];
                current++;
                g[i] = imageData[current];
                current++;
                r[i] = imageData[current];
                current += 2;
            }
        }

        BufferedImage toReturnImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics toReturnG = toReturnImage.getGraphics();

        int len = ((width * bitsPerPixel + 31) / 32) * 4;
        long skip = bitmapOffset - current;
        current += skip;

        // Draw the actual image, and collect the RGB histogram while iterating.
        int rawOffset = 0;
        byte[] rawData = new byte[len * height];
        for (int i = height - 1; i >= 0; i--) {

            // Collect the data per width.
            for (int j = rawOffset; j < (rawOffset + len) && current < imageData.length; j++) {
                rawData[j] = imageData[current];
                current++;
            }

            int k = rawOffset;
            int mask = 0xff;

            // Collect the data per pixle (pixle == 3 bytes) and draw the pixle
            // (as well as add the RGB histogram data).
            for (int l = 0; l < width; l++) {
                int b0 = (((int) (rawData[k++])) & mask);
                int b1 = (((int) (rawData[k++])) & mask) << 8;
                int b2 = (((int) (rawData[k++])) & mask) << 16;
                int rgbVal = 0xff000000 | b0 | b1 | b2;
                Color c = new Color(rgbVal);
                toReturnG.setColor(c);
                toReturnG.drawLine(l, i, l, i);
            }
            rawOffset += len;
        }

        toReturnG.dispose();
        return toReturnImage;
    }

    private int getInt(byte b1, byte b2, byte b3, byte b4) {
        return ((0xFF & b1) << 24) | ((0xFF & b2) << 16)
                | ((0xFF & b3) << 8) | (0xFF & b4);
    }

    private short getShort(byte b1, byte b2) {
        return (short) ((b1 << 8) + b2);
    }

    private void renderPanel(ImageItem img) {
        ((DrawPanel) drawPanel).setImage(img.getImg());
        jLabel1.setText(img.getDescription());
    }

    // Cache all panels to buffered images to draw.
    private void setupImages() {
        pages = new LinkedList<>();
        if (isBMPMode) {
            pages.clear();
            pages.add(getOriginalImage());
            pages.add(getCompressedImage());
            nextButton.setVisible(true);
            prevButton.setVisible(true);
            compressionRationLabelLabel.setVisible(true);
            compressionRationValueLabel.setVisible(true);
            saveButton.setVisible(true);
        } else {
            pages.clear();
            pages.add(getCompressedImage());
            nextButton.setVisible(false);
            prevButton.setVisible(false);
            compressionRationLabelLabel.setVisible(false);
            compressionRationValueLabel.setVisible(false);
            saveButton.setVisible(false);
        }
    }

    private ImageItem getOriginalImage() {
        return new ImageItem(createImageFromBytes(imageBytes), "Original Image", null);
    }

    private ImageItem getCompressedImage() {

        if (isBMPMode) {
            byte[] localCompressed = ImageCompression.compress(createImageFromBytes(imageBytes), isLossy);
            BufferedImage decompressed = ImageCompression.decompress(localCompressed);
            compressed = localCompressed;
            compressionRationValueLabel.setText(Double.toString((double) (imageBytes.length * Byte.SIZE) / (double) (localCompressed.length * Byte.SIZE) * 100) + "%");
            saveButton.setVisible(true);
            compressionRationLabelLabel.setVisible(true);
            compressionRationValueLabel.setVisible(true);
            return new ImageItem(decompressed, "Decompressed Image", null);
        } else {
            BufferedImage decompressed = ImageCompression.decompress(imageBytes);
            saveButton.setVisible(false);
            compressionRationLabelLabel.setVisible(false);
            compressionRationValueLabel.setVisible(false);
            return new ImageItem(decompressed, "Decompressed Image", null);
        }

    }

    private ImageItem getNext() {
        ImageItem toReturn = ((ImageItem) ((LinkedList) pages).removeFirst());
        ((LinkedList) pages).addLast(toReturn);
        return (ImageItem) ((LinkedList) pages).getFirst();
    }

    private ImageItem getPrev() {
        ImageItem toAdd = ((ImageItem) ((LinkedList) pages).removeLast());
        ((LinkedList) pages).addFirst(toAdd);
        return (ImageItem) ((LinkedList) pages).getFirst();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nextButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        canvas1 = new java.awt.Canvas();
        drawPanel = new DrawPanel();
        jLabel1 = new javax.swing.JLabel();
        prevButton = new javax.swing.JButton();
        compressionRationLabelLabel = new javax.swing.JLabel();
        compressionRationValueLabel = new javax.swing.JLabel();
        saveButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        nextButton.setText("Next");
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout drawPanelLayout = new javax.swing.GroupLayout(drawPanel);
        drawPanel.setLayout(drawPanelLayout);
        drawPanelLayout.setHorizontalGroup(
            drawPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 625, Short.MAX_VALUE)
        );
        drawPanelLayout.setVerticalGroup(
            drawPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 567, Short.MAX_VALUE)
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setText("[Description]");

        prevButton.setText("Prev");
        prevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevButtonActionPerformed(evt);
            }
        });

        compressionRationLabelLabel.setText("Compression Ratio:");

        compressionRationValueLabel.setText("[value]");

        saveButton.setText("Save Compressed Image");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(84, 84, 84)
                        .addComponent(drawPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nextButton)
                            .addComponent(prevButton)
                            .addComponent(closeButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(71, 71, 71)
                        .addComponent(canvas1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(203, 203, 203)
                .addComponent(compressionRationLabelLabel)
                .addGap(68, 68, 68)
                .addComponent(compressionRationValueLabel)
                .addGap(62, 62, 62)
                .addComponent(saveButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nextButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(prevButton)
                        .addGap(485, 485, 485)
                        .addComponent(closeButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(drawPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(canvas1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(compressionRationLabelLabel)
                    .addComponent(compressionRationValueLabel)
                    .addComponent(saveButton))
                .addGap(52, 52, 52))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void prevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevButtonActionPerformed
        renderPanel(getPrev());
    }//GEN-LAST:event_prevButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        renderPanel(getNext());
    }//GEN-LAST:event_nextButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        JFileChooser chooseWhereToSave = new JFileChooser(".");
        int selection = chooseWhereToSave.showSaveDialog(this);
        if (selection == JFileChooser.APPROVE_OPTION) {
            File f = chooseWhereToSave.getSelectedFile();
            try (FileOutputStream stream = new FileOutputStream(f.getAbsolutePath() + (isLossy ? ".im3" : ".in3"))) {
                stream.write(compressed);
            } catch (IOException ex) {

            }
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BMPViewer.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(() -> {
            BMPViewer dialog = new BMPViewer(new javax.swing.JFrame(), true, null, null, false, false);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });

    }

    // Used to store the next item to draw.
    private static class ImageItem {

        private final BufferedImage img;
        private final String description;
        private final TwoDimentionalGraphData twoDimentionalGraphData;

        public ImageItem(BufferedImage img, String description, TwoDimentionalGraphData twoDimentionalGraphData) {
            this.img = img;
            this.description = description;
            this.twoDimentionalGraphData = twoDimentionalGraphData;
        }

        public String getDescription() {
            return description;
        }

        public BufferedImage getImg() {
            return img;
        }

        public TwoDimentionalGraphData getTwoDimentionalGraphData() {
            return twoDimentionalGraphData;
        }
    }

// Used to store 2D graph data.
    private static class TwoDimentionalGraphData {

        private final float xMin;
        private final float yMin;
        private final float xMax;
        private final float yMax;
        private final String xLabel;
        private final String yLabel;

        public TwoDimentionalGraphData(float xMin, float yMin, float xMax, float yMax, String xLabel, String yLabel) {
            this.xMin = xMin;
            this.yMin = xMin;
            this.xMax = xMin;
            this.yMax = xMin;
            this.xLabel = xLabel;
            this.yLabel = yLabel;
        }

        public float getXMin() {
            return xMin;
        }

        public float getYMin() {
            return yMin;
        }

        public float getXMax() {
            return xMax;
        }

        public float getYMax() {
            return yMax;
        }

        public String getXLabel() {
            return xLabel;
        }

        public String getYLabel() {
            return yLabel;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Canvas canvas1;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel compressionRationLabelLabel;
    private javax.swing.JLabel compressionRationValueLabel;
    public javax.swing.JPanel drawPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton prevButton;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables
    private List<ImageItem> pages;
    private final byte[] imageBytes;
}
