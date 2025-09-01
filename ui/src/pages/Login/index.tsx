import { useEffect, type FC } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useGetUser, usePostUserLogin } from '@/api/apiComponents';
import { useUserContext } from '@/contexts/UserContext/User.context';
import { useApiKeyState } from '@/hooks/auth';
import {
  Form,
  FormControl,
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
import { UserIcon } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { toast } from 'sonner';
import { ErrorMessage } from '@/components/ErrorMessage';
import { NavLink } from 'react-router';

const isValidEmail = (value: string) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(value);
};

const schema = z.object({
  loginOrEmail: z
    .string()
    .min(1, 'Login or email is required')
    .refine((val) => isValidEmail(val) || val.length > 0, {
      message: 'Please enter a valid email or login.',
    }),
  password: z.string().min(1, 'Password is required'),
});

const FORM_ID = 'login-form';

export const Login: FC = () => {
  const form = useForm({
    resolver: zodResolver(schema),
    mode: 'onBlur',
    defaultValues: {
      loginOrEmail: '',
      password: '',
    },
  });

  const [apiKeyState, setApiKeyState] = useApiKeyState();

  const { dispatch } = useUserContext();

  const apiKey = apiKeyState?.apiKey;

  const { mutateAsync, error: postUserError } = usePostUserLogin({
    onSuccess: ({ apiKey }) => {
      setApiKeyState({ apiKey });
    },
  });

  const { data: user, isSuccess } = useGetUser(
    apiKey ? { headers: { Authorization: `Bearer ${apiKey}` } } : {},
    {
      enabled: Boolean(apiKey),
      retry: false,
    }
  );

  useEffect(() => {
    if (isSuccess) {
      dispatch({ type: 'LOG_IN', user });
      toast.success('Login successful!');
    }
  }, [user, dispatch, isSuccess]);

  return (
    <div className="flex items-center justify-center h-full">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <CardTitle>Login</CardTitle>
          <CardDescription>Enter your credentials to login</CardDescription>
          <CardAction>
            <UserIcon className="w-7 h-7 mt-2" />
          </CardAction>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form
              id={FORM_ID}
              className="grid grid-rows-2 gap-6"
              onSubmit={form.handleSubmit((data) =>
                mutateAsync({ body: data })
              )}
            >
              <FormField
                control={form.control}
                name="loginOrEmail"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Login or email</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="Enter your login or email"
                        {...field}
                      />
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
                      <Input
                        type="password"
                        placeholder="Enter your password"
                        {...field}
                      />
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
            Login
          </Button>
          <Button type="button" variant="link" asChild>
            <NavLink to="/recover-lost-password" className="ml-2">
              <span className="text-sm">Forgot password?</span>
            </NavLink>
          </Button>
          {postUserError && <ErrorMessage error={postUserError} />}
        </CardFooter>
      </Card>
    </div>
  );
};
