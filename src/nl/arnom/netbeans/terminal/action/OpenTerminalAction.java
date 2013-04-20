/*
 * Copyright (c) 2013, Arno Moonen.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package nl.arnom.netbeans.terminal.action;

import nl.arnom.netbeans.terminal.TerminalHelper;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

@ActionID(
        category = "Tools",
        id = "nl.arnom.netbeans.terminal.action.OpenTerminalAction")
@ActionRegistration(displayName = "Terminal From Here", lazy = false)
@ActionReferences({
    @ActionReference(path = "Loaders/folder/any/Actions", position = 1550),
    @ActionReference(path = "Projects/Actions"),
    @ActionReference(path = "Shortcuts", name = "C-T")
})
public final class OpenTerminalAction extends NodeAction {

    @Override
    protected void performAction(Node[] nodes) {
        if (nodes == null || nodes.length != 1) {
            return;
        }

        Node currentNode = nodes[0];
        FileObject file = null;

        // Selected a project?
        Project project = currentNode.getLookup().lookup(Project.class);
        if (project != null) {
            file = project.getProjectDirectory();
        }

        // Selected file or folder?
        if (file == null) {
            DataObject obj = currentNode.getCookie(DataObject.class);
            if (obj != null) {
                file = obj.getPrimaryFile();
            }
        }

        try {
            if (file != null) {
                // Get parent folder if 'file' is not a folder
                if (!file.isFolder()) {
                    file = file.getParent();
                }

                // Open terminal
                TerminalHelper.openTerminal(file.getPath());
            }
        } catch (Exception e) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("An error occured:\n" + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        }
    }

    @Override
    protected boolean enable(Node[] nodes) {

        if (nodes == null || nodes.length != 1) {
            return false;
        }

        Node currentNode = nodes[0];

        // Selected a project?
        if (currentNode.getLookup().lookup(Project.class) != null) {
            return true;
        }

        // Selected a file or folder?
        if (!currentNode.getCookie(DataObject.class).getPrimaryFile().isVirtual()) {
            return true;
        }

        return false;
    }

    @Override
    public String getName() {
        return "Terminal from here";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}