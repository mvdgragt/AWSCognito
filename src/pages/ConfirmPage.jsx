import { useState } from "react";
import { confirmSignUp } from "aws-amplify/auth";
import { useNavigate, useSearch } from "@tanstack/react-router";
export default function ConfirmPage() {
  const { email } = useSearch({ strict: false });
  const [code, setCode] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const handleConfirm = async (e) => {
    e.preventDefault();
    try {
      await confirmSignUp({ username: email, confirmationCode: code });
      navigate({ to: "/login" });
    } catch (err) {
      setError(err.message);
    }
  };
  return (
    <div>
      <h1>Bekräfta konto</h1>
      <p>En verifieringskod har skickats till {email}</p>
      <form onSubmit={handleConfirm}>
        <input
          type="text"
          placeholder="Ange 6-siffrig kod"
          value={code}
          onChange={(e) => setCode(e.target.value)}
          required
        />
        {error && <p style={{ color: "red" }}>{error}</p>}
        <button type="submit">Bekräfta</button>
      </form>
    </div>
  );
}
