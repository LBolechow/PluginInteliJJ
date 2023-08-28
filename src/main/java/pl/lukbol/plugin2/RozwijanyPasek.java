package pl.lukbol.plugin2;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.File;

public class RozwijanyPasek implements ToolWindowFactory {

    private DefaultListModel<String> listModel;
    private JBList<String> fileList;
    private VirtualFileListener fileListener;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        listModel = new DefaultListModel<>();
        updateFileList(project);

        fileList = new JBList<>(listModel);

        fileList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    String selectedFileName = fileList.getSelectedValue();
                    if (selectedFileName != null) {
                        VirtualFile selectedFile = LocalFileSystem.getInstance().findFileByPath(project.getBasePath() + "/" + selectedFileName);
                        if (selectedFile != null) {
                            OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, selectedFile);
                            openFileDescriptor.navigate(true);
                        }
                    }
                }
            }
        });

        fileListener = new VirtualFileAdapter() {
            @Override
            public void fileCreated(@NotNull VirtualFileEvent event) {
                updateFileList(project);
            }

            @Override
            public void fileDeleted(@NotNull VirtualFileEvent event) {
                updateFileList(project);
            }
        };

        VirtualFileManager.getInstance().addVirtualFileListener(fileListener);

        JBScrollPane scrollPane = new JBScrollPane(fileList);

        JBPanel panel = new JBPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);

    }

    private void updateFileList(Project project) {
        listModel.clear();
        String projectPath = project.getBasePath();
        if (projectPath != null) {
            File projectDir = new File(projectPath);
            if (projectDir.exists() && projectDir.isDirectory()) {
                File[] files = projectDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            listModel.addElement(file.getName());
                        }
                    }
                }
            }
        }
    }


}