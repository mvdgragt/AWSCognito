import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { fetchAuthSession } from "aws-amplify/auth";

const API_URL = import.meta.env.VITE_API_URL;

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

export function useDeleteAccount() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async () => {
      const headers = await getAuthHeaders();
      const res = await fetch(API_URL.replace("/todos", "/account"), {
        method: "DELETE",
        headers,
      });
      if (!res.ok) throw new Error("Kunde inte ta bort konto");
    },
    onSuccess: () => {
      queryClient.clear();
    },
  });
}
