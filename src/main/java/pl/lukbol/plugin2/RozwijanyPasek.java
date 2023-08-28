package pl.lukbol.plugin2;

import com.intellij.openapi.fileEditor.FileDocumentManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;

public class RozwijanyPasek implements ToolWindowFactory {

    private DefaultTreeModel treeModel;
    private Tree fileListTree;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(project.getBasePath());
        treeModel = new DefaultTreeModel(root);

        updateFileList(project, root, project.getBaseDir());

        fileListTree = new Tree(treeModel);

        VirtualFileListener fileListener = new VirtualFileListener() {
            @Override
            public void fileCreated(@NotNull VirtualFileEvent event) {
                updateFileList(project, root, project.getBaseDir());
            }

            @Override
            public void fileDeleted(@NotNull VirtualFileEvent event) {
                updateFileList(project, root, project.getBaseDir());
            }
        };

        VirtualFileManager.getInstance().addVirtualFileListener(fileListener);

        fileListTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath selectedPath = e.getPath();
                if (selectedPath != null) {
                    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
                    if (selectedNode != null) {
                        Object userObject = selectedNode.getUserObject();
                        if (userObject instanceof VirtualFile) {
                            VirtualFile selectedFile = (VirtualFile) userObject;
                            if (!selectedFile.isDirectory()) {
                                OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, selectedFile);
                                openFileDescriptor.navigate(true);
                            }
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JBScrollPane(fileListTree);

        JBPanel panel = new JBPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private void updateFileList(Project project, DefaultMutableTreeNode parent, VirtualFile directory) {
        parent.removeAllChildren();

        for (VirtualFile file : directory.getChildren()) {
            DefaultMutableTreeNode node;
            if (file.isDirectory()) {
                node = new DefaultMutableTreeNode(file);
                parent.add(node);
                updateFileList(project, node, file);
            } else {
                node = new DefaultMutableTreeNode(file);
                parent.add(node);
            }
        }

        treeModel.reload(parent);
    }
}