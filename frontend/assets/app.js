document.addEventListener("DOMContentLoaded", () => {
    const time = document.getElementById("load-time");
    const state = document.getElementById("backend-state");

    if (time) {
        time.textContent = `Página cargada: ${new Date().toLocaleTimeString("es-CL")}`;
    }

    // Apache expone /gateway-health como proxy hacia
    // http://127.0.0.1:9000/actuator/health en la instancia EC2.
    fetch("/gateway-health", { cache: "no-store" })
        .then((response) => {
            if (!response.ok) throw new Error("Gateway no disponible");
            return response.json();
        })
        .then((data) => {
            if (!state) return;
            const status = String(data.status || "UP").toUpperCase();
            state.textContent = status;
            state.classList.toggle("online", status === "UP");
            state.classList.toggle("offline", status !== "UP");
        })
        .catch(() => {
            if (!state) return;
            state.textContent = "OFFLINE";
            state.classList.add("offline");
        });
});
