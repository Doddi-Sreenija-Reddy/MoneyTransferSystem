import { Injectable } from '@angular/core';

export interface StatementRow {
  [key: string]: string | number;
}

@Injectable({ providedIn: 'root' })
export class StatementService {

  /**
   * Download data as a CSV file.
   */
  downloadCSV(filename: string, headers: string[], rows: string[][]): void {
    const escape = (val: string) =>
      `"${(val ?? '').toString().replace(/"/g, '""')}"`;

    const csvContent = [
      headers.map(escape).join(','),
      ...rows.map(row => row.map(escape).join(','))
    ].join('\r\n');

    const blob = new Blob(['\uFEFF' + csvContent], {
      type: 'text/csv;charset=utf-8;'
    });
    this.triggerDownload(blob, `${filename}.csv`);
  }

  /**
   * Print-to-PDF: Opens a print-optimised window with the data formatted
   * as an HTML table — no external dependencies needed.
   */
  downloadPDF(
    title: string,
    subtitle: string,
    headers: string[],
    rows: string[][]
  ): void {
    const tableRows = rows
      .map(row => `<tr>${row.map(cell => `<td>${cell}</td>`).join('')}</tr>`)
      .join('');

    const html = `
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8"/>
  <title>${title}</title>
  <style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap');
    * { margin:0; padding:0; box-sizing:border-box; }
    body { font-family: 'Inter', Arial, sans-serif; color: #1a3009; padding: 32px; }
    .header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:28px; border-bottom:3px solid #2d5016; padding-bottom:16px; }
    .brand { display:flex; align-items:center; gap:10px; }
    .brand-name { font-size:1.4rem; font-weight:700; color:#1a3009; }
    .brand-sub { font-size:0.8rem; color:#4a7c2c; }
    .doc-info { text-align:right; }
    .doc-title { font-size:1.1rem; font-weight:700; color:#2d5016; }
    .doc-date { font-size:0.8rem; color:#616161; margin-top:4px; }
    .subtitle { font-size:0.85rem; color:#616161; margin-bottom:20px; }
    table { width:100%; border-collapse:collapse; font-size:0.85rem; }
    thead tr { background:#2d5016; color:#fff; }
    thead th { padding:10px 12px; text-align:left; font-weight:600; font-size:0.8rem; text-transform:uppercase; letter-spacing:0.5px; }
    tbody tr:nth-child(even) { background:#f8f6f0; }
    tbody tr:hover { background:#f0ede3; }
    tbody td { padding:9px 12px; border-bottom:1px solid #e8e4d8; }
    .footer { margin-top:24px; font-size:0.75rem; color:#9e9e9e; text-align:center; border-top:1px solid #e0e0e0; padding-top:12px; }
    @media print {
      body { padding:16px; }
      button { display:none; }
    }
  </style>
</head>
<body>
  <div class="header">
    <div class="brand">
      <div>
        <div class="brand-name">💳 Money Transfer</div>
        <div class="brand-sub">Banking Statement</div>
      </div>
    </div>
    <div class="doc-info">
      <div class="doc-title">${title}</div>
      <div class="doc-date">Generated: ${new Date().toLocaleString('en-IN')}</div>
    </div>
  </div>
  <div class="subtitle">${subtitle}</div>
  <table>
    <thead><tr>${headers.map(h => `<th>${h}</th>`).join('')}</tr></thead>
    <tbody>${tableRows}</tbody>
  </table>
  <div class="footer">This is a system-generated statement. © Money Transfer System</div>
  <script>window.onload = () => { window.print(); };<\/script>
</body>
</html>`;

    const win = window.open('', '_blank');
    if (win) {
      win.document.write(html);
      win.document.close();
    } else {
      // Browser blocked the popup — guide user to allow it
      alert(
        'PDF could not open: your browser blocked the popup.\n\n' +
        'Please allow popups for this site and try again.\n' +
        '(Look for the popup-blocked icon in your address bar.)'
      );
    }
  }

  private triggerDownload(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }
}
