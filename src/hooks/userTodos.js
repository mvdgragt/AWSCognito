import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { fetchAuthSession } from "aws-amplify/auth";
const API_URL = "https://DIN_API_GATEWAY_URL/todos";
// Hämtar Access Token från Amplify för att bifoga i varje API-anrop
async function getAuthHeaders() {
  const session = await fetchAuthSession();
  const token = session.tokens.accessToken.toString();
  return {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  };
}
export function useTodos() {
  return useQuery({
    queryKey: ["todos"],
    queryFn: async () => {
      const headers = await getAuthHeaders();
      const res = await fetch(API_URL, { headers });
      if (!res.ok) throw new Error("Kunde inte hämta todos");
      return res.json();
    },
  });
}
export function useAddTodo() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (text) => {
      const headers = await getAuthHeaders();
      const res = await fetch(API_URL, {
        method: "POST",
        headers,
        body: JSON.stringify({ text }),
      });
      if (!res.ok) throw new Error("Kunde inte skapa todo");
      return res.json();
    },
    // Uppdatera cachen direkt utan att göra en ny GET-förfrågan
    onSuccess: (newTodo) => {
      queryClient.setQueryData(["todos"], (old) => [...(old || []), newTodo]);
    },
  });
}
export function useDeleteTodo() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (todoId) => {
      const headers = await getAuthHeaders();
      await fetch(API_URL, {
        method: "DELETE",
        headers,
        body: JSON.stringify({ todoId }),
      });
    },
    onSuccess: (_, todoId) => {
      queryClient.setQueryData(["todos"], (old) =>
        old.filter((t) => t.todoId !== todoId),
      );
    },
  });
}
