package org.skr.texturepackergui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by rat on 24.05.14.
 */

public class TexturePackerGui extends JFrame{

    private GdxApplication gApp;
    private TexturePackerProject project;

    private JPanel rootPanel;
    private JPanel gdxPanel;
    private JButton btnLoadProject;
    private JButton btnSaveProject;
    private JButton btnSaveProjectAs;
    private JButton btnNewProject;
    private JTextField tfInputDirectory;
    private JTextField tfOutputDirectory;
    private JButton btnBrowseInputDirectory;
    private JButton btnBrowseOutputDirectory;
    private JSpinner spinnerPaddingX;
    private JSpinner spinnerPaddingY;
    private JSpinner spinnerWidthMin;
    private JSpinner spinnerWidthMax;
    private JSpinner spinnerHeightMin;
    private JSpinner spinnerHeightMax;
    private JComboBox comboFilterMin;
    private JComboBox comboFilterMag;
    private JComboBox comboTWrapX;
    private JComboBox comboTWrapY;
    private JComboBox comboFormat;
    private JComboBox comboOutputFromat;
    private JComboBox comboJPegQuality;
    private JCheckBox chbPot;
    private JCheckBox chbBleed;
    private JCheckBox chbEdgePadding;
    private JCheckBox chbDuplicatePadding;
    private JCheckBox chbRotate;
    private JCheckBox chbFast;
    private JCheckBox chbStripWhitespaceX;
    private JCheckBox chbSquare;
    private JCheckBox chbStripWhitespaceY;
    private JCheckBox chbAlias;
    private JCheckBox chbDebug;
    private JCheckBox chbFlattenPaths;
    private JCheckBox chbUseIndexes;
    private JCheckBox chbCombineSubdirectories;
    private JCheckBox chbIgnoreBlankImages;
    private JCheckBox chbLimitMemory;
    private JCheckBox chbPremultiplyAlpha;
    private JCheckBox chbGrid;
    private JSpinner spinnerAlphaThreshold;
    private JTable tableScales;
    private JPanel panelScalesTable;
    private JButton btnInsertTableRow;
    private JButton btnRemTableRow;
    private JButton btnAddTableRow;
    private JButton btnPack;
    private JTextField tfPackFileName;
    private JComboBox comboPages;
    private JButton btnPrevPage;
    private JButton btnNextPage;
    private JComboBox comboRegions;
    private JButton btnShowRegion;


    class ScalesTableModel extends AbstractTableModel {


        Array< Float > scaleList = new Array< Float >();
        Array< String >suffixList = new Array<String>();


        public void updateDataFromProject() {
            scaleList.clear();
            suffixList.clear();

            TexturePacker.Settings s = project.getSettings();

            for ( int i = 0; i < s.scale.length; i++) {
                scaleList.add( s.scale[i]);
                suffixList.add( s.scaleSuffix[i]);
            }

            fireTableDataChanged();
        }


        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return getValueAt(0, columnIndex).getClass();
        }

        @Override
        public int getRowCount() {
            return scaleList.size;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {

            switch ( column ) {
                case 0:
                    return "Scale";
                case 1:
                    return "Suffix";
            }

            return "";
        }


        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {

            switch ( columnIndex ) {
                case 0:
                    return scaleList.get( rowIndex );
                case 1:
                    return suffixList.get( rowIndex );
            }

            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            switch ( columnIndex ) {
                case 0:
                    scaleList.set( rowIndex, (Float) aValue );
                    break;
                case 1:
                    suffixList.set( rowIndex, (String) aValue );
                    break;
            }

            fireTableCellUpdated( rowIndex, columnIndex );
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public void addRow( int index ) {

            if ( index < 0) {
                scaleList.add(1f);
                suffixList.add("");
            } else {
                scaleList.insert(index, 1f);
                suffixList.insert(index, "");
            }

            fireTableDataChanged();
        }

        public void remRow( int index ) {

            if ( index < 0)
                return;
            if ( index >= scaleList.size)
                return;

            if ( scaleList.size == 1)
                return;

            scaleList.removeIndex( index );
            suffixList.removeIndex( index );
            fireTableDataChanged();
        }

        void updateProjectFromData() {
            TexturePacker.Settings s = project.getSettings();

            String [] suffix = new String[ suffixList.size ];

            float [] scale = new float[ scaleList.size ];

            for (int i = 0; i < scaleList.size; i++) {
                scale[i] = scaleList.get(i);
                suffix[i] = new String( suffixList.get(i) );
            }

            s.scale = scale;
            s.scaleSuffix = suffix;
        }
    }

    private ScalesTableModel scalesTableModel = new ScalesTableModel();

    public TexturePackerGui() {


        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(rootPanel);

        project = new TexturePackerProject();
        scalesTableModel.updateDataFromProject();

        tableScales.setModel( scalesTableModel );
        JTableHeader th = tableScales.getTableHeader();

        panelScalesTable.add(th, BorderLayout.NORTH);



        gApp = new GdxApplication();
        final LwjglAWTCanvas gdxCanvas = new LwjglAWTCanvas( gApp );
        gdxPanel.add(gdxCanvas.getCanvas(), BorderLayout.CENTER);
        pack();
        setSize(1280, 900);

        gApp.setAtlasLoadListener( new GdxApplication.AtlasLoadListener() {
            @Override
            public void atlasLoaded() {
                onAtlasLoaded();
            }
        });


        ApplicationSettings.loadSettings();

        loadGuiFromSettings();


        addWindowListener( new ThisWindowListener() );


        btnLoadProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadProject();
            }
        });
        btnSaveProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveProject( false );
            }
        });
        btnSaveProjectAs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveProject( true );
            }
        });
        btnNewProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newProject();
            }

        });
        btnBrowseInputDirectory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseDirectory( tfInputDirectory );
            }
        });
        btnBrowseOutputDirectory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseDirectory( tfOutputDirectory );
            }
        });


        btnInsertTableRow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scalesTableModel.addRow(tableScales.getSelectedRow());
            }
        });
        btnRemTableRow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scalesTableModel.remRow( tableScales.getSelectedRow() );
            }
        });
        btnAddTableRow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scalesTableModel.addRow( -1 );
            }
        });
        btnPack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                packProject();
            }
        });
        comboPages.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setCurrentPage();
            }
        });
        btnPrevPage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int cp = comboPages.getSelectedIndex();
                if (cp <= 0)
                    return;
                comboPages.setSelectedIndex(cp - 1);
                setCurrentPage();
            }
        });
        btnNextPage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int cp = comboPages.getSelectedIndex();
                if (cp >= ( comboPages.getItemCount() - 1 ) )
                    return;
                comboPages.setSelectedIndex(cp + 1);
                setCurrentPage();
            }
        });
        btnShowRegion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegion();
            }
        });
    }


    void showRegion() {

        Gdx.app.postRunnable( new Runnable() {
            @Override
            public void run() {
                int cr = comboRegions.getSelectedIndex();
                String name = gApp.getRegionNames().get(cr);

                gApp.showRegion( name );
            }
        });



    }

    void setCurrentPage() {
        Gdx.app.postRunnable( new Runnable() {
            @Override
            public void run() {
                gApp.setCurrentPage( comboPages.getSelectedIndex() );
            }
        });
    }


    void packProject() {

        updateProjectFromGui();

        File fl = new File( ApplicationSettings.getProjectFile() );


        String packFileName = project.getPackFileName();

        if ( packFileName.isEmpty() ) {
            return;
        }

        Gdx.app.log("TexturePackerGui.packProject", "Pack file: " + packFileName );

        boolean allOk = true;

        try {
            TexturePacker.process(project.getSettings(), project.getInputDirectory(), project.getOutputDirectory(), packFileName);
        } catch (RuntimeException e ) {
            Gdx.app.error("TexturePackerGui.packProject", " ... Fail", e );
            allOk = false;
        }

        if ( allOk ) {
            Gdx.app.log("TexturePackerGui.packProject", " ... Done" );
            updateAtlas();
        }

    }



    void browseDirectory( JTextField tf ) {
         final JFileChooser fch = new JFileChooser();

        fch.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );

        int res = fch.showDialog( null, "Select");

        if ( res != JFileChooser.APPROVE_OPTION )
            return;

        File dir = fch.getSelectedFile();

        tf.setText( dir.getAbsolutePath() );

        updateProjectFromGui();
    }


    void loadGuiFromSettings() {

        String pf = ApplicationSettings.getProjectFile();

        if (pf.compareTo("noname") == 0)
            return;

        File fl = new File(pf);

        if (!fl.exists())
            return;

        loadProjectFromFile( pf );

        updateGuiFromProject();
    }


    void updateTitle() {
        setTitle( " TexturePackerGui: " + ApplicationSettings.getProjectFile() );
    }


    void updateGuiFromProject() {

        updateTitle();

        tfInputDirectory.setText( project.getInputDirectory() );
        tfOutputDirectory.setText( project.getOutputDirectory() );
        tfPackFileName.setText( project.getPackFileName() );

        TexturePacker.Settings s = project.getSettings();

        spinnerHeightMax.setValue( s.maxHeight );
        spinnerHeightMin.setValue( s.minHeight );
        spinnerPaddingX.setValue( s.paddingX );
        spinnerPaddingY.setValue( s.paddingY );
        spinnerWidthMax.setValue( s.maxWidth );
        spinnerWidthMin.setValue( s.minWidth );

        comboFilterMin.setSelectedIndex( getIndexOfFilter( s.filterMin) );
        comboFilterMag.setSelectedIndex( getIndexOfFilter( s.filterMag) );

        comboTWrapX.setSelectedIndex( getIndexOfWrap( s.wrapX ) );
        comboTWrapY.setSelectedIndex( getIndexOfWrap( s.wrapY ) );

        comboFormat.setSelectedIndex( getIndexOfFormat( s.format ) );

        if ( s.outputFormat.compareTo("png") == 0) {
            comboOutputFromat.setSelectedIndex( 0 );
        } else {
            comboOutputFromat.setSelectedIndex( 1 );
        }

        comboJPegQuality.setSelectedIndex( (int) (s.jpegQuality * 10 - 1) );
        spinnerAlphaThreshold.setValue( s.alphaThreshold );


        chbPot.setSelected( s.pot );
        chbBleed.setSelected( s.bleed );
        chbEdgePadding.setSelected( s.edgePadding );
        chbDuplicatePadding.setSelected( s.duplicatePadding );
        chbRotate.setSelected( s.rotation );
        chbFast.setSelected( s.fast );
        chbStripWhitespaceX.setSelected( s.stripWhitespaceX );
        chbStripWhitespaceY.setSelected( s.stripWhitespaceY );
        chbSquare.setSelected( s.square );
        chbAlias.setSelected( s.alias );
        chbDebug.setSelected( s.debug );
        chbFlattenPaths.setSelected( s.flattenPaths );
        chbUseIndexes.setSelected( s.useIndexes );
        chbCombineSubdirectories.setSelected( s.combineSubdirectories );
        chbIgnoreBlankImages.setSelected( s.ignoreBlankImages );
        chbLimitMemory.setSelected( s.limitMemory );
        chbPremultiplyAlpha.setSelected( s.premultiplyAlpha );
        chbGrid.setSelected( s.grid );

        scalesTableModel.updateDataFromProject();

        updateAtlas();

    };



    void updateAtlas() {


        Gdx.app.postRunnable( new Runnable() {
            @Override
            public void run() {

                String packFileName = project.getPackFileName();

                if ( packFileName.isEmpty() ) {
                    return;
                }

                //TODO: think about suffixes

                if (packFileName.indexOf('.') == -1 || packFileName.toLowerCase().endsWith(".png")
                        || packFileName.toLowerCase().endsWith(".jpg")) {
                    packFileName += ".atlas";
                }

                gApp.updateAtlas( project.getOutputDirectory() + File.separator + packFileName );

            }
        });



    }


    void updateProjectFromGui() {
        project.setInputDirectory( tfInputDirectory.getText() );
        project.setOutputDirectory( tfOutputDirectory.getText() );
        project.setPackFileName( tfPackFileName.getText() );

        TexturePacker.Settings s = project.getSettings();

        s.minHeight = (Integer) spinnerHeightMin.getValue();
        s.maxHeight = (Integer) spinnerHeightMax.getValue();
        s.maxWidth = (Integer) spinnerWidthMax.getValue();
        s.minWidth = (Integer) spinnerWidthMin.getValue();
        s.paddingX = (Integer) spinnerPaddingX.getValue();
        s.paddingY = (Integer) spinnerPaddingY.getValue();

        s.filterMin = getFilterByIndex( comboFilterMin.getSelectedIndex() );
        s.filterMag = getFilterByIndex( comboFilterMag.getSelectedIndex() );

        s.wrapX = getTextureWrapByIndex( comboTWrapX.getSelectedIndex() );
        s.wrapY = getTextureWrapByIndex( comboTWrapY.getSelectedIndex() );

        s.format = getFormatByIndex( comboFormat.getSelectedIndex() );

        if ( comboOutputFromat.getSelectedIndex() == 0) {
            s.outputFormat = "png";
        } else {
            s.outputFormat = "jpg";
        }

        s.jpegQuality = (comboJPegQuality.getSelectedIndex() + 1) / 10f;
        s.alphaThreshold = (Integer) spinnerAlphaThreshold.getValue();


        s.pot = chbPot.isSelected();
        s.edgePadding = chbEdgePadding.isSelected();
        s.duplicatePadding = chbDuplicatePadding.isSelected();
        s.rotation = chbRotate.isSelected();
        s.square = chbSquare.isSelected();
        s.stripWhitespaceX = chbStripWhitespaceX.isSelected();
        s.stripWhitespaceY = chbStripWhitespaceY.isSelected();
        s.alias = chbAlias.isSelected();
        s.ignoreBlankImages = chbIgnoreBlankImages.isSelected();
        s.fast = chbFast.isSelected();
        s.debug = chbDebug.isSelected();
        s.combineSubdirectories = chbCombineSubdirectories.isSelected();
        s.flattenPaths = chbFlattenPaths.isSelected();
        s.premultiplyAlpha = chbPremultiplyAlpha.isSelected();
        s.useIndexes = chbUseIndexes.isSelected();
        s.bleed = chbBleed.isSelected();
        s.limitMemory = chbLimitMemory.isSelected();
        s.grid = chbGrid.isSelected();

        scalesTableModel.updateProjectFromData();
    };


    int getIndexOfFormat ( Pixmap.Format format ) {

        switch ( format ) {

            case Alpha:
                return 0;
            case Intensity:
                return 1;
            case LuminanceAlpha:
                return 2;
            case RGB565:
                return 3;
            case RGBA4444:
                return 4;
            case RGB888:
                return 5;
            case RGBA8888:
                return 6;
        }

        return 6;
    }


    Pixmap.Format getFormatByIndex( int index ) {

        switch ( index ) {
            case 0:
                return Pixmap.Format.Alpha;
            case 1:
                return Pixmap.Format.Intensity;
            case 2:
                return Pixmap.Format.LuminanceAlpha;
            case 3:
                return Pixmap.Format.RGB565;
            case 4:
                return Pixmap.Format.RGBA4444;
            case 5:
                return Pixmap.Format.RGB888;
            case 6:
                return Pixmap.Format.RGBA8888;

        }

        return Pixmap.Format.RGBA8888;
    }


    int getIndexOfWrap( Texture.TextureWrap tw ) {
        switch ( tw ) {

            case MirroredRepeat:
                return 0;
            case ClampToEdge:
                return 1;
            case Repeat:
                return 2;
        }

        return 0;
    }

    Texture.TextureWrap getTextureWrapByIndex( int index ) {

        switch ( index ) {
            case 0:
                return Texture.TextureWrap.MirroredRepeat;
            case 1:
                return Texture.TextureWrap.ClampToEdge;
            case 2:
                return Texture.TextureWrap.Repeat;
        }

        return Texture.TextureWrap.MirroredRepeat;
    }



    int getIndexOfFilter(Texture.TextureFilter f) {
        switch ( f ) {

            case Nearest:
                return 0;
            case Linear:
                return 1;
            case MipMap:
                return 2;
            case MipMapNearestNearest:
                return 3;
            case MipMapLinearNearest:
                return 4;
            case MipMapNearestLinear:
                return 5;
            case MipMapLinearLinear:
                return 6;
        }

        return 0;
    }

    Texture.TextureFilter getFilterByIndex(int index ) {

        switch ( index ) {
            case 0:
                return Texture.TextureFilter.Nearest;
            case 1:
                return Texture.TextureFilter.Linear;
            case 2:
                return Texture.TextureFilter.MipMap;
            case 3:
                return Texture.TextureFilter.MipMapNearestNearest;
            case 4:
                return Texture.TextureFilter.MipMapLinearNearest;
            case 5:
                return Texture.TextureFilter.MipMapNearestLinear;
            case 6:
                return Texture.TextureFilter.MipMapLinearLinear;
        }

        return Texture.TextureFilter.Nearest;
    }


    void newProject() {
        ApplicationSettings.setProjectFile( "noname" );

        project = new TexturePackerProject();

        updateGuiFromProject();
    }



    void loadProject() {
        final JFileChooser fch = new JFileChooser();

        int res = fch.showDialog( null, "Open" );

        if ( res != JFileChooser.APPROVE_OPTION)
            return;
        Gdx.app.log("TexturePackerGui.loadProject", "File: " + fch.getSelectedFile() );


        File fl = fch.getSelectedFile();
        if (!fl.exists())
            return;

        loadProjectFromFile( fl.getAbsolutePath() );

        updateGuiFromProject();
    }

    void loadProjectFromFile( String file ) {

        Json js = new Json();

        FileHandle fh = Gdx.files.absolute( file );

        project = js.fromJson( TexturePackerProject.class, fh );

        Gdx.app.log("TexturePackerGui.loadProject", "Project loaded from: " + fh );

        ApplicationSettings.setProjectFile( file );

    }

    void saveProject(boolean saveAs) {

        if ( ApplicationSettings.getProjectFile().compareTo("noname") == 0) {
            saveAs = true;
        }

        File fl;

        if ( saveAs ) {
            final JFileChooser fch = new JFileChooser();

            int res = fch.showDialog( null, "Save As" );

            if ( res != JFileChooser.APPROVE_OPTION)
                return;
            fl = fch.getSelectedFile();
        } else {
            fl = new File(ApplicationSettings.getProjectFile());
        }


        updateProjectFromGui();

        Json js = new Json();

//        Gdx.app.log("TexturePackerGui.saveProject: ", "JSON: " + js.prettyPrint(project));

        FileHandle fh = Gdx.files.absolute( fl.getAbsolutePath() );
//        fh.writeString(js.toJson(project), false);

        js.toJson(project, TexturePackerProject.class, fh);

        Gdx.app.log("TexturePackerGui.saveProject: ", " project saved");
        ApplicationSettings.setProjectFile( fl.getAbsolutePath() );

        updateTitle();
    }

    public static void main(String [] args) {

        System.out.println("TexturePackerGui. ");

        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                TexturePackerGui instance = new TexturePackerGui();
                instance.setVisible(true);
            }
        });
    }


    void onAtlasLoaded() {
        comboPages.removeAllItems();

        int c = gApp.getPagesCount();

        for (int i = 0; i < c; i++) {
            comboPages.addItem( "Page: " + i);
        }

        comboRegions.removeAllItems();

        for ( String s : gApp.getRegionNames() )
            comboRegions.addItem( s );

    }

}
