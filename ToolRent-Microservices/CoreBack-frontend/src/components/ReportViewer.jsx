import React, { useEffect, useState } from "react";
import ReportService from "../services/report.service";

function ReportViewer() {
  const [reportType, setReportType] = useState("LATE_CLIENTS");
  const [reportData, setReportData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(""); 
  const [dateRange, setDateRange] = useState({ from: "", to: "" });
  const [useDateFilter, setUseDateFilter] = useState(false);

  // --- AUTENTICACIÓN JWT ---
  const token = localStorage.getItem("token");
  const isAuth = !!token;
  // Permitir ver reportes a ADMIN y USER/EMPLEADO
  let canView = false;
  if (isAuth) {
      try {
          const payload = JSON.parse(atob(token.split('.')[1]));
          const role = payload.role;
          canView = role === "ADMIN" || role === "USER" || role === "EMPLEADO";
      } catch (e) { console.error(e); }
  }
  // -------------------------

  const loadReport = (type, filterDates) => {
    if (!canView) return;
    setLoading(true);
    setReportData([]);
    setMessage("");

    const fromDate = filterDates && dateRange.from ? dateRange.from : null;
    const toDate = filterDates && dateRange.to ? dateRange.to : null;

    if (filterDates && type !== "TOP_TOOLS" && (!fromDate || !toDate)) {
        setMessage("Error: Start and End dates are required.");
        setLoading(false);
        return;
    }
    if (type === "TOP_TOOLS" && (!fromDate || !toDate)) {
        setMessage("Error: Date range is mandatory for Top Tools.");
        setLoading(false);
        return;
    }

    let promise;
    switch (type) {
      case "ACTIVE_LOANS":
      case "LATE_LOANS":
        promise = ReportService.getLoansByStatus(type.replace('_LOANS', ''), fromDate, toDate);
        break;
      case "LATE_CLIENTS":
        promise = ReportService.getClientsWithLateLoans(fromDate, toDate);
        break;
      case "TOP_TOOLS":
        promise = ReportService.getTopTools(fromDate, toDate);
        break;
      default:
        setMessage("Invalid report type.");
        setLoading(false);
        return;
    }

    promise
      .then(response => {
        setReportData(response.data);
        if (response.data.length === 0) setMessage("No results found.");
      })
      .catch(e => {
          console.error("Error:", e);
          setMessage("Error loading report.");
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    if (canView) loadReport(reportType, false);
  }, [canView]);

  const handleRunReport = (type) => {
    setReportType(type);
    loadReport(type, useDateFilter);
  };

  const renderData = () => {
    if (loading) return <p>Loading...</p>;
    if (message && reportData.length === 0) return <p>{message}</p>;
    if (reportData.length === 0) return <p>No data available.</p>;

    switch (reportType) {
      case "ACTIVE_LOANS":
      case "LATE_LOANS":
        return (
          <table border="1" style={{width: '100%', borderCollapse: 'collapse'}}>
            <thead><tr><th>ID</th><th>Client</th><th>Tool</th><th>Start</th><th>Due</th><th>Status</th></tr></thead>
            <tbody>
              {reportData.map(loan => (
                <tr key={loan.id}>
                  <td>{loan.id}</td><td>{loan.client?.name}</td><td>{loan.tool?.name}</td><td>{loan.startDate}</td><td>{loan.dueDate}</td><td>{loan.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        );
      case "LATE_CLIENTS":
         return (
          <table border="1" style={{width: '100%', borderCollapse: 'collapse'}}>
            <thead><tr><th>ID</th><th>RUT</th><th>Name</th><th>Email</th><th>Status</th></tr></thead>
            <tbody>
              {reportData.map(client => (
                <tr key={client.id}>
                  <td>{client.id}</td><td>{client.rut}</td><td>{client.name}</td><td>{client.email}</td><td>{client.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        );
      case "TOP_TOOLS":
        return (
          <table border="1" style={{width: '100%', borderCollapse: 'collapse'}}>
            <thead><tr><th>Rank</th><th>Tool</th><th>Loans</th></tr></thead>
            <tbody>
              {reportData.map((item, index) => (
                <tr key={index}>
                  <td>{index + 1}</td><td>{item[0]?.name}</td><td>{item[1]}</td>
                </tr>
              ))}
            </tbody>
          </table>
        );
      default: return null;
    }
  };

  if (!canView) return <p style={{padding:16}}>Access Denied.</p>;

  return (
    <div style={{ padding: 16 }}>
      <h2>System Reports</h2>
      <div style={{ marginBottom: '20px', border: '1px solid #ccc', padding: '10px' }}>
        <label><input type="checkbox" checked={useDateFilter} onChange={(e) => setUseDateFilter(e.target.checked)}/> Apply Date Filter</label>
        <div style={{ marginTop: '10px', opacity: useDateFilter ? 1 : 0.5 }}>
          <label>From: </label><input type="date" value={dateRange.from} onChange={e => setDateRange({ ...dateRange, from: e.target.value })} disabled={!useDateFilter}/>
          <label style={{marginLeft:'10px'}}>To: </label><input type="date" value={dateRange.to} onChange={e => setDateRange({ ...dateRange, to: e.target.value })} disabled={!useDateFilter}/>
        </div>
      </div>

      <div style={{ display: 'flex', gap: '10px' }}>
        <button onClick={() => handleRunReport("LATE_CLIENTS")}>Late Clients</button>
        <button onClick={() => handleRunReport("ACTIVE_LOANS")}>Active Loans</button>
        <button onClick={() => handleRunReport("LATE_LOANS")}>Late Loans</button>
        <button onClick={() => handleRunReport("TOP_TOOLS")}>Top Tools</button>
      </div>

      <div style={{ marginTop: '20px' }}>{renderData()}</div>
    </div>
  );
}

export default ReportViewer;