import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { usePostUserChangepassword } from 'api/apiComponents';
import { useApiKeyState } from 'hooks/auth';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useForm } from 'react-hook-form';
import z from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import type { FC } from 'react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';

const schema = z.object({
  currentPassword: z.string().min(8),
  newPassword: z.string().min(8),
  repeatedPassword: z.string().min(8),
});

export const PasswordDetails: FC = () => {
  const [storageApiKeyState, setStorageApiKeyState] = useApiKeyState();

  const form = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      currentPassword: '',
      newPassword: '',
      repeatedPassword: '',
    },
  });

  const mutation = usePostUserChangepassword({
    onSuccess: ({ apiKey: newApiKey }) => {
      setStorageApiKeyState({ apiKey: newApiKey });
    },
  });

  const apiKey = storageApiKeyState?.apiKey;

  return (
    <Card>
      <CardHeader>
        <CardTitle>Password details</CardTitle>
      </CardHeader>
      <CardContent>
        {apiKey ? (
          <Form {...form}>
            <form
              id="password-details-form"
              className="grid grid-rows-4 gap-6"
              onSubmit={form.handleSubmit((body) => mutation.mutate({ body }))}
            >
              <FormField
                control={form.control}
                name="currentPassword"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Current password</FormLabel>
                    <FormControl>
                      <Input type="password" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="newPassword"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>New password</FormLabel>
                    <FormControl>
                      <Input type="password" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="repeatedPassword"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Repeat new password</FormLabel>
                    <FormControl>
                      <Input type="password" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <Button
                type="submit"
                className="w-full"
                disabled={form.formState.isSubmitting}
              >
                Update password details
              </Button>
            </form>
          </Form>
        ) : (
          <h3 className="mb-4">Password details not available</h3>
        )}
      </CardContent>
    </Card>
  );
};
