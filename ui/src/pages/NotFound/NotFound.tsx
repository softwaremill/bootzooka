import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { BsGithub } from 'react-icons/bs';
import { Link, NavLink } from 'react-router';

export const NotFound: React.FC = () => (
  <div className="w-full h-full flex flex-col items-center justify-center">
    <Card className="w-full max-w-sm">
      <CardHeader>
        <CardTitle>Ooops!</CardTitle>
        <CardDescription>
          You shouldn't be here for sure 😅 <br />
        </CardDescription>
      </CardHeader>
      <CardContent className="grid grid-rows-4 gap-4">
        <p className="text-sm">Please choose one of the locations below:</p>

        <Button asChild>
          <NavLink to="/">Home page</NavLink>
        </Button>
        <Button asChild variant="outline">
          <Link
            to="https://softwaremill.com"
            rel="noopener noreferrer"
            target="_blank"
          >
            SoftwareMill - our company website
          </Link>
        </Button>
        <Button asChild variant="outline">
          <Link
            to="https://github.com/softwaremill/bootzooka/"
            rel="noopener noreferrer"
            target="_blank"
          >
            <BsGithub className="w-6 h-6" />
            Bootzooka on GitHub
          </Link>
        </Button>
      </CardContent>
    </Card>
  </div>
);
