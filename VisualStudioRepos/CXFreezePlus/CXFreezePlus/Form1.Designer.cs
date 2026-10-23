namespace CXFreezePlus {
    partial class Form1 {
        /// <summary>
        ///  Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        ///  Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing) {
            if (disposing && (components != null)) {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        ///  Required method for Designer support - do not modify
        ///  the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent() {
            this.PATH = new System.Windows.Forms.TextBox();
            this.label1 = new System.Windows.Forms.Label();
            this.MODE = new System.Windows.Forms.GroupBox();
            this.PROXY_B = new System.Windows.Forms.RadioButton();
            this.FREEZE_B = new System.Windows.Forms.RadioButton();
            this.OPEN_PATH = new System.Windows.Forms.Button();
            this.FILE_DIALOG = new System.Windows.Forms.OpenFileDialog();
            this.RUN = new System.Windows.Forms.Button();
            this.HISTORY_OPEN_BTN = new System.Windows.Forms.Button();
            this.ARGS = new System.Windows.Forms.TextBox();
            this.label2 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.MODE.SuspendLayout();
            this.SuspendLayout();
            // 
            // PATH
            // 
            this.PATH.Location = new System.Drawing.Point(54, 12);
            this.PATH.Name = "PATH";
            this.PATH.Size = new System.Drawing.Size(175, 23);
            this.PATH.TabIndex = 0;
            this.PATH.TextChanged += new System.EventHandler(this.PATH_TextChanged);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(13, 16);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(35, 15);
            this.label1.TabIndex = 1;
            this.label1.Text = "PATH";
            // 
            // MODE
            // 
            this.MODE.Controls.Add(this.PROXY_B);
            this.MODE.Controls.Add(this.FREEZE_B);
            this.MODE.Location = new System.Drawing.Point(13, 70);
            this.MODE.Name = "MODE";
            this.MODE.Size = new System.Drawing.Size(293, 87);
            this.MODE.TabIndex = 2;
            this.MODE.TabStop = false;
            this.MODE.Text = "MODE";
            // 
            // PROXY_B
            // 
            this.PROXY_B.AutoSize = true;
            this.PROXY_B.Location = new System.Drawing.Point(21, 51);
            this.PROXY_B.Name = "PROXY_B";
            this.PROXY_B.Size = new System.Drawing.Size(62, 19);
            this.PROXY_B.TabIndex = 1;
            this.PROXY_B.TabStop = true;
            this.PROXY_B.Text = "PROXY";
            this.PROXY_B.UseVisualStyleBackColor = true;
            this.PROXY_B.CheckedChanged += new System.EventHandler(this.PROXY_B_CheckedChanged);
            // 
            // FREEZE_B
            // 
            this.FREEZE_B.AutoSize = true;
            this.FREEZE_B.Location = new System.Drawing.Point(21, 26);
            this.FREEZE_B.Name = "FREEZE_B";
            this.FREEZE_B.Size = new System.Drawing.Size(63, 19);
            this.FREEZE_B.TabIndex = 0;
            this.FREEZE_B.TabStop = true;
            this.FREEZE_B.Text = "FREEZE";
            this.FREEZE_B.UseVisualStyleBackColor = true;
            this.FREEZE_B.CheckedChanged += new System.EventHandler(this.FREEZE_B_CheckedChanged);
            // 
            // OPEN_PATH
            // 
            this.OPEN_PATH.Location = new System.Drawing.Point(236, 12);
            this.OPEN_PATH.Name = "OPEN_PATH";
            this.OPEN_PATH.Size = new System.Drawing.Size(69, 23);
            this.OPEN_PATH.TabIndex = 3;
            this.OPEN_PATH.Text = "OPEN";
            this.OPEN_PATH.UseVisualStyleBackColor = true;
            this.OPEN_PATH.Click += new System.EventHandler(this.OPEN_PATH_Click);
            // 
            // FILE_DIALOG
            // 
            this.FILE_DIALOG.FileName = "java.exe";
            // 
            // RUN
            // 
            this.RUN.Location = new System.Drawing.Point(12, 163);
            this.RUN.Name = "RUN";
            this.RUN.Size = new System.Drawing.Size(218, 31);
            this.RUN.TabIndex = 4;
            this.RUN.Text = "RUN ANIWAY";
            this.RUN.UseVisualStyleBackColor = true;
            this.RUN.Click += new System.EventHandler(this.RUN_Click);
            // 
            // HISTORY_OPEN_BTN
            // 
            this.HISTORY_OPEN_BTN.Location = new System.Drawing.Point(236, 163);
            this.HISTORY_OPEN_BTN.Name = "HISTORY_OPEN_BTN";
            this.HISTORY_OPEN_BTN.Size = new System.Drawing.Size(70, 31);
            this.HISTORY_OPEN_BTN.TabIndex = 5;
            this.HISTORY_OPEN_BTN.Text = "HISTORY";
            this.HISTORY_OPEN_BTN.UseVisualStyleBackColor = true;
            this.HISTORY_OPEN_BTN.Click += new System.EventHandler(this.HISTORY_OPEN_BTN_Click);
            // 
            // ARGS
            // 
            this.ARGS.Location = new System.Drawing.Point(54, 41);
            this.ARGS.Name = "ARGS";
            this.ARGS.Size = new System.Drawing.Size(251, 23);
            this.ARGS.TabIndex = 6;
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(13, 44);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(36, 15);
            this.label2.TabIndex = 7;
            this.label2.Text = "ARGS";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(13, 44);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(36, 15);
            this.label3.TabIndex = 7;
            this.label3.Text = "ARGS";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(12, 16);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(35, 15);
            this.label4.TabIndex = 1;
            this.label4.Text = "PATH";
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.AutoSize = true;
            this.ClientSize = new System.Drawing.Size(318, 203);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.ARGS);
            this.Controls.Add(this.HISTORY_OPEN_BTN);
            this.Controls.Add(this.RUN);
            this.Controls.Add(this.OPEN_PATH);
            this.Controls.Add(this.MODE);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.PATH);
            this.Name = "Form1";
            this.Text = "CXFreezePlus - Main";
            this.MODE.ResumeLayout(false);
            this.MODE.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private TextBox PATH;
        private Label label1;
        private GroupBox MODE;
        private RadioButton PROXY_B;
        private RadioButton FREEZE_B;
        private Button OPEN_PATH;
        private OpenFileDialog FILE_DIALOG;
        private Button RUN;
        private Button HISTORY_OPEN_BTN;
        private TextBox ARGS;
        private Label label2;
        private Label label3;
        private Label label4;
    }
}