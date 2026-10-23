namespace CXFreezePlus {
    partial class Form3 {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
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
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent() {
            this.TEXT = new System.Windows.Forms.TextBox();
            this.CLEAR_BTN = new System.Windows.Forms.Button();
            this.SAVE_DIALOG = new System.Windows.Forms.SaveFileDialog();
            this.SAVE_BTN = new System.Windows.Forms.Button();
            this.AUTO_SAVE_CB = new System.Windows.Forms.CheckBox();
            this.button3 = new System.Windows.Forms.Button();
            this.OPEN_DIALOG = new System.Windows.Forms.OpenFileDialog();
            this.SuspendLayout();
            // 
            // TEXT
            // 
            this.TEXT.Location = new System.Drawing.Point(12, 0);
            this.TEXT.Multiline = true;
            this.TEXT.Name = "TEXT";
            this.TEXT.ReadOnly = true;
            this.TEXT.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.TEXT.Size = new System.Drawing.Size(676, 329);
            this.TEXT.TabIndex = 0;
            // 
            // CLEAR_BTN
            // 
            this.CLEAR_BTN.Location = new System.Drawing.Point(606, 335);
            this.CLEAR_BTN.Name = "CLEAR_BTN";
            this.CLEAR_BTN.Size = new System.Drawing.Size(82, 44);
            this.CLEAR_BTN.TabIndex = 1;
            this.CLEAR_BTN.Text = "CLEAR";
            this.CLEAR_BTN.UseVisualStyleBackColor = true;
            this.CLEAR_BTN.Click += new System.EventHandler(this.CLEAR_BTN_Click);
            // 
            // SAVE_DIALOG
            // 
            //this.SAVE_DIALOG.FileOk += new System.ComponentModel.CancelEventHandler(this.SAVE_DIALOG_FileOk);
            // 
            // SAVE_BTN
            // 
            this.SAVE_BTN.Location = new System.Drawing.Point(112, 335);
            this.SAVE_BTN.Name = "SAVE_BTN";
            this.SAVE_BTN.Size = new System.Drawing.Size(488, 44);
            this.SAVE_BTN.TabIndex = 2;
            this.SAVE_BTN.Text = "SAVE";
            this.SAVE_BTN.UseVisualStyleBackColor = true;
            this.SAVE_BTN.Click += new System.EventHandler(this.SAVE_BTN_Click);
            // 
            // AUTO_SAVE_CB
            // 
            this.AUTO_SAVE_CB.AutoSize = true;
            this.AUTO_SAVE_CB.Location = new System.Drawing.Point(13, 359);
            this.AUTO_SAVE_CB.Name = "AUTO_SAVE_CB";
            this.AUTO_SAVE_CB.Size = new System.Drawing.Size(93, 19);
            this.AUTO_SAVE_CB.TabIndex = 3;
            this.AUTO_SAVE_CB.Text = "AUTO - SAVE";
            this.AUTO_SAVE_CB.UseVisualStyleBackColor = true;
            this.AUTO_SAVE_CB.CheckedChanged += new System.EventHandler(this.AUTO_SAVE_CB_CheckedChanged);
            // 
            // button3
            // 
            this.button3.Location = new System.Drawing.Point(12, 335);
            this.button3.Name = "button3";
            this.button3.Size = new System.Drawing.Size(93, 23);
            this.button3.TabIndex = 4;
            this.button3.Text = "PATH";
            this.button3.UseVisualStyleBackColor = true;
            this.button3.Click += new System.EventHandler(this.button3_Click);
            // 
            // Form3
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(700, 383);
            this.Controls.Add(this.button3);
            this.Controls.Add(this.AUTO_SAVE_CB);
            this.Controls.Add(this.SAVE_BTN);
            this.Controls.Add(this.CLEAR_BTN);
            this.Controls.Add(this.TEXT);
            this.Name = "Form3";
            this.Text = "CXFreeze - History";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private TextBox TEXT;
        private Button CLEAR_BTN;
        private SaveFileDialog SAVE_DIALOG;
        private Button SAVE_BTN;
        private CheckBox AUTO_SAVE_CB;
        private Button button3;
        private OpenFileDialog OPEN_DIALOG;
    }
}