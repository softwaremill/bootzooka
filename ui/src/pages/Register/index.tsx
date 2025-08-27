import type { FC } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { usePostUserRegister } from '@/api/apiComponents';
import { useApiKeyState } from '@/hooks/auth';
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import {
  Card,
  CardAction,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { UserRoundPlusIcon } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { toast } from 'sonner';
import { ErrorMessage } from '@/components/ErrorMessage';

const schema = z
  .object({
    login: z
      .string('Login is required')
      .min(3, 'Login must be at least 3 characters long'),
    email: z.email('Email is required'),
    password: z.string('Password is required').min(5),
    repeatedPassword: z.string('Please repeat your password'),
  })
  .refine((data) => data.password === data.repeatedPassword, {
    message: 'Passwords do not match',
    path: ['repeatedPassword'],

    when(payload) {
      return schema
        .pick({ password: true, repeatedPassword: true })
        .safeParse(payload.value).success;
    },
  });

const FORM_ID = 'registration-form';

export const Register: FC = () => {
  const [, setApiKeyState] = useApiKeyState();

  const { mutate, error } = usePostUserRegister({
    onSuccess: ({ apiKey }) => {
      setApiKeyState({ apiKey });
      toast.success('Registration successful! Welcome aboard!');
    },
  });

  const form = useForm({
    resolver: zodResolver(schema),
    mode: 'onBlur',
    defaultValues: {
      login: '',
      email: '',
      password: '',
      repeatedPassword: '',
    },
  });

  return (
    <div className="flex items-center justify-center h-full">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <CardTitle>Register</CardTitle>
          <CardDescription>Enter your credentials to register</CardDescription>
          <CardAction>
            <UserRoundPlusIcon className="w-7 h-7 mt-2" />
          </CardAction>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form
              id={FORM_ID}
              className="grid grid-rows-2 gap-6"
              onSubmit={form.handleSubmit((data) => mutate({ body: data }))}
            >
              <FormField
                control={form.control}
                name="login"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Login</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="email"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Email</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Password</FormLabel>
                    <FormControl>
                      <Input type="password" {...field} />
                    </FormControl>
                    <FormDescription>
                      Password must be at least 5 characters long
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="repeatedPassword"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Repeat Password</FormLabel>
                    <FormControl>
                      <Input type="password" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </form>
          </Form>
        </CardContent>
        <CardFooter className="grid grid-rows-2 gap-4">
          <Button
            type="submit"
            form={FORM_ID}
            className="w-full"
            disabled={form.formState.isSubmitting}
          >
            Create new account
          </Button>
          {error && <ErrorMessage error={error} />}
        </CardFooter>
      </Card>
    </div>
  );
};
