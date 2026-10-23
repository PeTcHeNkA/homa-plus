namespace CXFreezePlus {
    partial class Form2 {
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
            this.BTN = new System.Windows.Forms.Button();
            this.CONSOLE = new System.Windows.Forms.TextBox();
            this.INPUT = new System.Windows.Forms.TextBox();
            this.AUTO = new System.Windows.Forms.CheckBox();
            this.OUT = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // BTN
            // 
            this.BTN.Location = new System.Drawing.Point(533, 194);
            this.BTN.Name = "BTN";
            this.BTN.Size = new System.Drawing.Size(213, 56);
            this.BTN.TabIndex = 0;
            this.BTN.Text = "RUN";
            this.BTN.UseVisualStyleBackColor = true;
            this.BTN.Click += new System.EventHandler(this.BTN_Click);
            // 
            // CONSOLE
            // 
            this.CONSOLE.BackColor = System.Drawing.SystemColors.ControlDarkDark;
            this.CONSOLE.ForeColor = System.Drawing.SystemColors.Info;
            this.CONSOLE.Location = new System.Drawing.Point(12, 0);
            this.CONSOLE.MaxLength = 999999999;
            this.CONSOLE.Multiline = true;
            this.CONSOLE.Name = "CONSOLE";
            this.CONSOLE.ReadOnly = true;
            this.CONSOLE.RightToLeft = System.Windows.Forms.RightToLeft.No;
            this.CONSOLE.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.CONSOLE.Size = new System.Drawing.Size(734, 188);
            this.CONSOLE.TabIndex = 1;
            // 
            // INPUT
            // 
            this.INPUT.Location = new System.Drawing.Point(12, 194);
            this.INPUT.MaxLength = 99999999;
            this.INPUT.Multiline = true;
            this.INPUT.Name = "INPUT";
            this.INPUT.RightToLeft = System.Windows.Forms.RightToLeft.No;
            this.INPUT.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.INPUT.Size = new System.Drawing.Size(512, 81);
            this.INPUT.TabIndex = 2;
            // 
            // AUTO
            // 
            this.AUTO.AutoSize = true;
            this.AUTO.Location = new System.Drawing.Point(536, 255);
            this.AUTO.Name = "AUTO";
            this.AUTO.Size = new System.Drawing.Size(93, 19);
            this.AUTO.TabIndex = 3;
            this.AUTO.Text = "AUTO MODE";
            this.AUTO.UseVisualStyleBackColor = true;
            this.AUTO.CheckedChanged += new System.EventHandler(this.AUTO_CheckedChanged);
            // 
            // OUT
            // 
            this.OUT.Location = new System.Drawing.Point(644, 252);
            this.OUT.Name = "OUT";
            this.OUT.Size = new System.Drawing.Size(102, 23);
            this.OUT.TabIndex = 4;
            this.OUT.Text = "OUT";
            this.OUT.UseVisualStyleBackColor = true;
            // 
            // Form2
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.AutoSize = true;
            this.ClientSize = new System.Drawing.Size(756, 279);
            this.Controls.Add(this.OUT);
            this.Controls.Add(this.AUTO);
            this.Controls.Add(this.INPUT);
            this.Controls.Add(this.CONSOLE);
            this.Controls.Add(this.BTN);
            this.Name = "Form2";
            this.Text = "CXFreezePlus - Console";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private Button BTN;
        private TextBox INPUT;
        private CheckBox AUTO;
        private Button OUT;
        private TextBox CONSOLE;
    }
}